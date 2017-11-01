/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.solr.util;

import com.dataspark.api.data.GeoHierarchy;
import com.dataspark.api.data.GeoNodeDepth;
import com.dataspark.api.data.GeoNodeType;
import com.dataspark.api.data.RoiLevelInfo;
import com.dataspark.api.data.RoiNode;
import com.dataspark.api.data.TelcoPlanningConstants;
import com.dataspark.api.exception.ServiceException;
import com.dataspark.api.service.TelcoPlanningServiceHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author ragarwal, 2017-05-03
 */
@Service("geoJsonGenerator")
@Log4j
public class GeoJsonGenerator {

  @Value("${telcoplanning.geojson.file}")
  String geoJsonFile;

  @Value("${telcoplanning.geojson.hierarchy}")
  String geoJsonHierarchy;

  @Value("${telcoplanning.geojson.updated}")
  String geoJsonUpdated;

  public void generateGeoJsonUpdated() throws ServiceException {
    Writer writer = null;
    try {
      // 1. Read and parse the hierarchy file starting from nation
      File hierarchyFile = new File(geoJsonHierarchy);
      GeoHierarchy hierarchy = new ObjectMapper().readValue(hierarchyFile, GeoHierarchy.class);
      List<RoiNode> children =
          parseHierarchyRecursively(hierarchy.getChildren(), "", GeoNodeDepth.roiL0);

      // 2. Create and populate the nation node information
      RoiNode nation = new RoiNode();
      nation.setDisplayType(hierarchy.getDisplayType().toString().toLowerCase());
      Map<String, Object> roiFilters = new HashMap<String, Object>();
      roiFilters.put(hierarchy.getGeoNodeDepth().toString(), Arrays.asList(new String[] {"*"}));
      nation.setRoiFilters(roiFilters);
      nation.setId(hierarchy.getGeoNodeDepth().getRoiName().replace(
          TelcoPlanningConstants.Delimiters.DASH, TelcoPlanningConstants.Delimiters.UNDERSCORE));
      nation.setLabel(hierarchy.getGeoNodeDepth().getRoiName().toUpperCase().replace(
          TelcoPlanningConstants.Delimiters.DASH, TelcoPlanningConstants.Delimiters.SPACE));
      nation.setChildren(children);

      String nationJson =
          JsonUtil.OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(nation);
      log.debug("geo json updated : \n" + nationJson);

      // 3. Write the nation geo json tree to a file
      File geoJsonUpdatedFile = new File(geoJsonUpdated);
      writer = new BufferedWriter(
          new OutputStreamWriter(new FileOutputStream(geoJsonUpdatedFile), StandardCharsets.UTF_8));
      writer.write(nationJson);

    } catch (Exception e) {
      log.error("Error in generating geo json : ", e);
      throw new ServiceException(e.getMessage(), e);
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          // This is unrecoverable.
          e.printStackTrace();
        }
      }
    }
  }

  private List<RoiNode> parseHierarchyRecursively(List<GeoHierarchy> hierarchyMOs, String parentId,
      GeoNodeDepth parentDepth) throws IOException {
    List<RoiNode> roiNodeMOs = new ArrayList<RoiNode>();
    parentId = parentId.replace(TelcoPlanningConstants.Delimiters.DASH,
        TelcoPlanningConstants.Delimiters.UNDERSCORE);
    if (!CollectionUtils.isEmpty(hierarchyMOs)) {
      for (GeoHierarchy hierarchy : hierarchyMOs) {

        if (GeoNodeType.ROI_LEVEL.equals(hierarchy.getGeoNodeType())) {
          RoiNode roiNode = processRoiLevel(parentId, parentDepth, hierarchy);
          roiNodeMOs.add(roiNode);
        } else if (GeoNodeType.ROI_VALUE.equals(hierarchy.getGeoNodeType())) {
          roiNodeMOs.addAll(processRoiValue(parentId, parentDepth, hierarchy));
        }

      }
    }
    return roiNodeMOs;
  }

  private Collection<? extends RoiNode> processRoiValue(String parentId, GeoNodeDepth parentDepth,
      GeoHierarchy hierarchy) throws IOException {
    List<RoiNode> roiNodeMOs = new ArrayList<RoiNode>();
    // Update the parent info, if parent is absent in the input geo json tree

    if (GeoNodeDepth.absentinGeoJson.contains(parentDepth)) {
      parentDepth = hierarchy.getGeoNodeDepth();
      parentId = TelcoPlanningConstants.UNKNOWN_ID;
    }

    // For ROI_VALUE, fetch all the values at the specified level from the geo json tree
    String query = new TelcoPlanningServiceHelper().buildQuery(parentId, parentDepth.name(),
        hierarchy.getGeoNodeDepth().name(), GeoNodeDepth.getRoiLevelsMap());
    List<RoiLevelInfo> roiInfoMOs = new ArrayList<RoiLevelInfo>();
    File file = new File(geoJsonFile);
    String treeJson = FileUtils.readFileToString(file, "UTF-8");
    roiInfoMOs = JsonPath.parse(treeJson).read(query, roiInfoMOs.getClass());

    // For each value, create and populate the node information
    for (int i = 0; i < roiInfoMOs.size(); i++) {
      RoiLevelInfo roiInfo = new ObjectMapper().convertValue(roiInfoMOs.get(i), RoiLevelInfo.class);
      RoiNode roiNode =
          populateRoiNode(geoJsonFile, hierarchy, roiInfo.getId(), hierarchy.getGeoNodeDepth());
      roiNode.setLabel(hierarchy.getDisplayType() + TelcoPlanningConstants.Delimiters.COLON
          + roiInfo.getName().replace(TelcoPlanningConstants.Delimiters.DASH,
              TelcoPlanningConstants.Delimiters.SPACE));

      Map<String, Object> roiFilters = new HashMap<String, Object>();
      roiFilters.put(hierarchy.getGeoNodeDepth().toString(),
          Arrays
              .asList(new String[] {roiInfo.getId().replace(TelcoPlanningConstants.Delimiters.DASH,
                  TelcoPlanningConstants.Delimiters.UNDERSCORE)}));
      roiNode.setRoiFilters(roiFilters);
      roiNode.setId(roiInfo.getId().replace(TelcoPlanningConstants.Delimiters.DASH,
          TelcoPlanningConstants.Delimiters.UNDERSCORE));
      roiNode.setNodeValueIsA(hierarchy.getGeoNodeDepth().getRoiName());
      roiNodeMOs.add(roiNode);
    }

    return roiNodeMOs;
  }

  private RoiNode processRoiLevel(String parentId, GeoNodeDepth parentDepth, GeoHierarchy hierarchy)
      throws IOException {
    // For ROI_LEVEL, create and populate the node information directly

    RoiNode roiNode = populateRoiNode(geoJsonFile, hierarchy, TelcoPlanningConstants.UNKNOWN_ID,
        hierarchy.getGeoNodeDepth());
    roiNode.setLabel(hierarchy.getDisplayType() + TelcoPlanningConstants.Delimiters.COLON
        + hierarchy.getGeoNodeDepth().getRoiName().toUpperCase().replace(
            TelcoPlanningConstants.Delimiters.DASH, TelcoPlanningConstants.Delimiters.SPACE));

    Map<String, Object> roiFilters = new HashMap<String, Object>();
    roiFilters.put(hierarchy.getGeoNodeDepth().toString(), Arrays.asList(new String[] {"*"}));
    roiFilters.put(parentDepth.toString(), Arrays.asList(new String[] {parentId}));
    roiNode.setId(hierarchy.getGeoNodeDepth().toString());

    // For "All sites" level, reset label and roiFilters
    if (GeoNodeDepth.roiL5.equals(hierarchy.getGeoNodeDepth())) {
      roiFilters = new HashMap<String, Object>();
      roiFilters.put(parentDepth.toString(), Arrays.asList(new String[] {parentId}));
      roiNode.setLabel(hierarchy.getGeoNodeDepth().getRoiName().toUpperCase().replace(
          TelcoPlanningConstants.Delimiters.UNDERSCORE, TelcoPlanningConstants.Delimiters.SPACE));
    }

    roiNode.setRoiFilters(roiFilters);
    return roiNode;
  }

  private RoiNode populateRoiNode(String geoJsonFile, GeoHierarchy hierarchy, String parentId,
      GeoNodeDepth parentDepth) throws IOException {
    // Populate common fields for both ROI_LEVEL and ROI_VALUE

    RoiNode roiNode = new RoiNode();
    roiNode.setDisplayType(hierarchy.getDisplayType().toString().toLowerCase());
    roiNode.setAggregatableAt(
        hierarchy.getAggregatableAt() == null ? "" : hierarchy.getAggregatableAt().toString());
    roiNode.setChildren(parseHierarchyRecursively(hierarchy.getChildren(), parentId, parentDepth));

    return roiNode;
  }
}
