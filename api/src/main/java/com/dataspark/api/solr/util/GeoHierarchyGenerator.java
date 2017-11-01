/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.solr.util;

import com.dataspark.api.data.GeoDisplayType;
import com.dataspark.api.data.GeoHierarchy;
import com.dataspark.api.data.GeoNodeDepth;
import com.dataspark.api.data.GeoNodeType;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author ragarwal, 2017-05-03
 */

public class GeoHierarchyGenerator {

  public void generateHierarchy() throws IOException {
    GeoHierarchy kabupaten = new GeoHierarchy(GeoNodeType.ROI_LEVEL, GeoNodeDepth.roiL2,
        GeoDisplayType.STATS, GeoNodeDepth.roiL2, null);
    GeoHierarchy kecamatan = new GeoHierarchy(GeoNodeType.ROI_LEVEL, GeoNodeDepth.roiL3,
        GeoDisplayType.STATS, GeoNodeDepth.roiL3, null);
    GeoHierarchy kelurahan = new GeoHierarchy(GeoNodeType.ROI_LEVEL, GeoNodeDepth.roiL4,
        GeoDisplayType.STATS, GeoNodeDepth.roiL4, null);
    GeoHierarchy allSites = new GeoHierarchy(GeoNodeType.ROI_LEVEL, GeoNodeDepth.roiL5,
        GeoDisplayType.SITES, null, null);

    GeoHierarchy bbcValues = new GeoHierarchy(GeoNodeType.ROI_VALUE, GeoNodeDepth.roiL1,
        GeoDisplayType.STATS, GeoNodeDepth.roiL2,
        Arrays.asList(new GeoHierarchy[] {kabupaten, kecamatan, kelurahan, allSites}));

    GeoHierarchy nation =
        new GeoHierarchy(GeoNodeType.ROI_LEVEL, GeoNodeDepth.roiL0, GeoDisplayType.STATS,
            GeoNodeDepth.roiL1, Arrays.asList(new GeoHierarchy[] {bbcValues}));

    String geoHierarchy =
        JsonUtil.OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(nation);

    File geoHierarchyFile = new File("geoHierarchy.json");
    Writer writer = new BufferedWriter(
        new OutputStreamWriter(new FileOutputStream(geoHierarchyFile), StandardCharsets.UTF_8));
    writer.write(geoHierarchy);
    writer.close();
  }

  // public static void main(String[] args) throws IOException {
  // new GeoHierarchyGenerator().generateHierarchy();
  // }

}
