/*
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.service;

import static java.util.Collections.unmodifiableMap;

import com.dataspark.api.data.TelcoPlanningConstants;
import com.dataspark.api.solr.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author ragarwal, 2017-06-13
 */
@Service
@Log4j
public class SolrToJsonPropMappingService {

  // solrField to jsonProp map
  public final Map<String, String> cellCollSiteInfoMap;
  public final Map<String, String> cellCollCellInfoMap;
  public final Map<String, String> roiStatsCollInfoMap;
  public final Map<String, String> userTxnCollProfileInfoMap;

  // jsonProp to index map
  public final Map<String, Integer> siteInfoJsonPropIndexMap;
  public final Map<String, Integer> userProfileJsonPropIndexMap;

  public final Map<String, String> cellCollIndicatorsFormulaMap = new HashMap<String, String>();
  public final Map<String, String> cellCollIndicatorsAggTypeMap = new HashMap<String, String>();
  public final List<String> userProfileLookupProps = new ArrayList<String>();

  public final Map<String, String> roiStatsJsonPropHeaderMap;

  // user transaction faceted query indicators
  public final Map<String, Map<String, Object>> userTxnCalculatedIndicatorsMap;

  private SolrToJsonPropMappingService(
      @Value("${telcoplanning.jsonmapping.solr}") String mappingFile) {
    Map<String, String> cellCollSiteInfoMap = new HashMap<>();
    Map<String, String> cellCollCellInfoMap = new HashMap<>();
    Map<String, String> roiStatsCollInfoMap = new HashMap<>();
    Map<String, String> userTxnCollProfileInfoMap = new HashMap<>();
    Map<String, String> roiStatsJsonPropHeaderMap = new HashMap<>();

    Map<String, Integer> siteInfoJsonPropIndexMap = new HashMap<>();
    Map<String, Integer> userProfileJsonPropIndexMap = new HashMap<>();

    Map<String, Map<String, Object>> userTxnCalculatedIndicatorsMap = new HashMap<>();

    try {
      InputStream stream = new FileInputStream(new File(mappingFile));
      JsonNode map = JsonUtil.OBJECT_MAPPER.readTree(stream);

      // 1. parse cell collection details
      JsonNode cellColl = map.get(TelcoPlanningConstants.SolrToJsonPropMap.CELL_COLLECTION);

      // populate cell collection site info map
      JsonNode ccSiteInfo =
          cellColl.get(TelcoPlanningConstants.SolrToJsonPropMap.CELL_COLL_SITE_INFO);
      cellCollSiteInfoMap.putAll(getSolrToJsonPropMapping(ccSiteInfo,
          TelcoPlanningConstants.SolrToJsonPropMap.CELL_COLL_SITE_INFO));

      // populate cell collection site info map
      JsonNode ccCellInfo =
          cellColl.get(TelcoPlanningConstants.SolrToJsonPropMap.CELL_COLL_CELL_INFO);
      cellCollCellInfoMap.putAll(getSolrToJsonPropMapping(ccCellInfo,
          TelcoPlanningConstants.SolrToJsonPropMap.CELL_COLL_CELL_INFO));

      // 2. parse roi stats collection details
      JsonNode roiStatsColl =
          map.get(TelcoPlanningConstants.SolrToJsonPropMap.ROI_STATS_COLLECTION);
      roiStatsCollInfoMap.putAll(getSolrToJsonPropMapping(roiStatsColl,
          TelcoPlanningConstants.SolrToJsonPropMap.ROI_STATS_COLLECTION));

      // 3. parse user txn collection details
      JsonNode userTxnColl = map.get(TelcoPlanningConstants.SolrToJsonPropMap.USER_TXN_COLLECTION);

      // populate user txn collection parent info map
      JsonNode userTxnCollUserProfile =
          userTxnColl.get(TelcoPlanningConstants.SolrToJsonPropMap.USER_TXN_COLL_USER_PROFILE);
      userTxnCollProfileInfoMap.putAll(getSolrToJsonPropMapping(userTxnCollUserProfile,
          TelcoPlanningConstants.SolrToJsonPropMap.USER_TXN_COLL_USER_PROFILE));

      // populate user transaction indicators
      JsonNode userTxnCollIndicators =
          userTxnColl.get(TelcoPlanningConstants.SolrToJsonPropMap.USER_TXN_COLL_USER_TXN);
      userTxnCalculatedIndicatorsMap.putAll(getUserTxnCollIndicators(userTxnCollIndicators));

      // 4. populate site info header, index map
      JsonNode siteInfoHeader = map.get(TelcoPlanningConstants.SolrToJsonPropMap.SITE_INFO_HEADER);
      siteInfoJsonPropIndexMap.putAll(getHeaderIndexMap(siteInfoHeader));

      // 5. populate user profile header, index map
      userProfileJsonPropIndexMap.putAll(getHeaderIndexMap(userTxnCollUserProfile));

      // 6. populate roi stats jsonProp, header map
      roiStatsJsonPropHeaderMap.putAll(getJsonPropHeaderMap(roiStatsColl));

      stream.close();
    } catch (IOException e) {
      log.error("Failed to start up json to solr field mapping service");
      throw new RuntimeException("Failed to startup json to solr field mapping service: ", e);
    }
    this.cellCollSiteInfoMap = unmodifiableMap(cellCollSiteInfoMap);
    this.cellCollCellInfoMap = unmodifiableMap(cellCollCellInfoMap);
    this.roiStatsCollInfoMap = unmodifiableMap(roiStatsCollInfoMap);
    this.userTxnCollProfileInfoMap = unmodifiableMap(userTxnCollProfileInfoMap);
    this.siteInfoJsonPropIndexMap = unmodifiableMap(siteInfoJsonPropIndexMap);
    this.userProfileJsonPropIndexMap = unmodifiableMap(userProfileJsonPropIndexMap);
    this.roiStatsJsonPropHeaderMap = unmodifiableMap(roiStatsJsonPropHeaderMap);
    this.userTxnCalculatedIndicatorsMap = unmodifiableMap(userTxnCalculatedIndicatorsMap);
  }

  private Map<String, Map<String, Object>> getUserTxnCollIndicators(JsonNode mapping) {
    Map<String, Map<String, Object>> userTxnIndicators = new HashMap<>();

    // map of indicator json property and other info.
    for (JsonNode val : mapping) {
      JsonNode jsonProperty = val.get(TelcoPlanningConstants.SolrToJsonPropMap.JSON_PROPERTY);
      JsonNode isIndicator = val.get(TelcoPlanningConstants.SolrToJsonPropMap.IS_INDICATOR);
      JsonNode facetName = val.get(TelcoPlanningConstants.SolrToJsonPropMap.FACET_NAME);
      JsonNode formula = val.get(TelcoPlanningConstants.SolrToJsonPropMap.FORMULA);

      if (jsonProperty == null || (isIndicator != null && isIndicator.asBoolean()
          && (formula == null || facetName == null))) {
        log.error("Input file format is wrong. Will try to recover.");
        continue;
      }

      Map<String, Object> indicatorInfo = new HashMap<>();
      indicatorInfo.put(TelcoPlanningConstants.SolrToJsonPropMap.IS_INDICATOR,
          isIndicator == null ? false : isIndicator.booleanValue());
      indicatorInfo.put(TelcoPlanningConstants.SolrToJsonPropMap.FACET_NAME,
          facetName == null ? "" : facetName.asText());
      indicatorInfo.put(TelcoPlanningConstants.SolrToJsonPropMap.FORMULA,
          formula == null ? "" : formula.asText());

      userTxnIndicators.put(jsonProperty.asText(), indicatorInfo);
    }

    return userTxnIndicators;
  }

  private Map<String, String> getJsonPropHeaderMap(JsonNode mapping) {
    Map<String, String> jsonPropHeaderMap = new HashMap<String, String>();
    for (JsonNode val : mapping) {
      JsonNode jsonProperty = val.get(TelcoPlanningConstants.SolrToJsonPropMap.JSON_PROPERTY);
      JsonNode header = val.get(TelcoPlanningConstants.SolrToJsonPropMap.COLUMN_HEADER);
      if (header == null || jsonProperty == null) {
        log.error("Input file format is wrong. Will try to recover.");
        continue;
      }
      jsonPropHeaderMap.put(jsonProperty.asText(), header.asText());
    }
    return jsonPropHeaderMap;
  }

  private Map<String, Integer> getHeaderIndexMap(JsonNode mapping) {
    Map<String, Integer> headerIndexMap = new HashMap<String, Integer>();
    for (JsonNode val : mapping) {
      JsonNode jsonProperty = val.get(TelcoPlanningConstants.SolrToJsonPropMap.JSON_PROPERTY);
      JsonNode index = val.get(TelcoPlanningConstants.SolrToJsonPropMap.INDEX);
      if (index == null || jsonProperty == null) {
        log.error("Input file format is wrong. Will try to recover.");
        continue;
      }
      headerIndexMap.put(jsonProperty.asText(), index.asInt());
    }
    return headerIndexMap;
  }

  private Map<String, String> getSolrToJsonPropMapping(JsonNode mapping, String name) {
    Map<String, String> solrToJsonPropMap = new HashMap<>();
    for (JsonNode val : mapping) {
      JsonNode solrField = val.get(TelcoPlanningConstants.SolrToJsonPropMap.SOLR_FIELD);
      JsonNode jsonProperty = val.get(TelcoPlanningConstants.SolrToJsonPropMap.JSON_PROPERTY);
      JsonNode isIndicator = val.get(TelcoPlanningConstants.SolrToJsonPropMap.IS_INDICATOR);
      JsonNode doLookup = val.get(TelcoPlanningConstants.SolrToJsonPropMap.DO_LOOKUP);
      JsonNode formula = val.get(TelcoPlanningConstants.SolrToJsonPropMap.FORMULA);
      JsonNode aggType = val.get(TelcoPlanningConstants.SolrToJsonPropMap.AGGREGATION_TYPE);

      if (solrField == null || jsonProperty == null) {
        log.error("Input file format is wrong for the field " + name + ". Will try to recover.");
        continue;
      }

      // parse indicators and populate respective maps.
      if (isIndicator != null && isIndicator.booleanValue()) {
        switch (name) {
          case TelcoPlanningConstants.SolrToJsonPropMap.CELL_COLL_CELL_INFO:
            cellCollIndicatorsFormulaMap.put(jsonProperty.asText(),
                formula == null ? "" : formula.asText());
            cellCollIndicatorsAggTypeMap.put(jsonProperty.asText(),
                aggType == null ? TelcoPlanningConstants.AggregationType.SUM : aggType.asText());
            break;
        }
      }

      // save hint to convert segment and device info from number to actual string value.
      if (doLookup != null && doLookup.booleanValue()
          && TelcoPlanningConstants.SolrToJsonPropMap.USER_TXN_COLL_USER_PROFILE.equals(name)) {
        userProfileLookupProps.add(solrField.asText());
      }

      solrToJsonPropMap.put(solrField.asText(), jsonProperty.asText());
    }
    return solrToJsonPropMap;
  }
}
