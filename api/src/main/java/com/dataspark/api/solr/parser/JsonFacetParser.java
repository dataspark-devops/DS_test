/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.solr.parser;

import com.dataspark.api.data.TelcoPlanningConstants;
import com.dataspark.api.service.SolrToJsonPropMappingService;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author ragarwal, 2017-03-07
 */
@Component
@SuppressWarnings("unchecked")
public class JsonFacetParser {

  @Autowired
  private SolrToJsonPropMappingService solrToJsonPropSvc;

  public String buildJsonFacetForHomeWorkResults(List<String> requestedIndicators) {
    JSONObject jsonFacetObject = new JSONObject();

    if (requestedIndicators.contains(TelcoPlanningConstants.Indicators.USER_TXN_PARENT_HOME)) {
      JSONObject home_sites_facet = new JSONObject();
      home_sites_facet.put("type", "terms");
      home_sites_facet.put("field", "home_site_id_s");
      home_sites_facet.put("limit", -1);
      jsonFacetObject.put("home_sites", home_sites_facet);
    }

    if (requestedIndicators.contains(TelcoPlanningConstants.Indicators.USER_TXN_PARENT_WORK)) {
      JSONObject work_sites_facet = new JSONObject();
      work_sites_facet.put("type", "terms");
      work_sites_facet.put("field", "work_site_id_s");
      work_sites_facet.put("limit", -1);
      jsonFacetObject.put("work_sites", work_sites_facet);
    }

    return jsonFacetObject.toJSONString();
  }

  public String buildJsonFacetForCellStats(String id, List<String> requestedIndicators) {
    JSONObject jsonFacetObject = new JSONObject();
    JSONObject aggregated_facets = new JSONObject();

    if (requestedIndicators.contains(TelcoPlanningConstants.Indicators.USER_TXN_CHILD_UNIQUE_PEOPLE)
        || requestedIndicators
            .contains(TelcoPlanningConstants.Indicators.USER_TXN_CHILD_AVG_STAY_DURATION)) {

      JSONObject blockParent = new JSONObject();
      blockParent.put("blockParent", id + ":*");

      JSONObject hllId = new JSONObject();
      hllId.put("unique_count", "hll(" + id + ")");

      JSONObject unique_count_facet_child = new JSONObject();
      unique_count_facet_child.put("type", "query");
      unique_count_facet_child.put("q", "*:*");
      unique_count_facet_child.put("domain", blockParent);
      unique_count_facet_child.put("facet", hllId);
      aggregated_facets.put("unique_count", unique_count_facet_child);
    }

    aggregated_facets.putAll(addUserTxnIndicatorFacets(requestedIndicators));

    JSONObject all_cells_facet_child = new JSONObject();
    all_cells_facet_child.put("type", "terms");
    all_cells_facet_child.put("field", "cell_id_s");
    all_cells_facet_child.put("limit", -1);
    all_cells_facet_child.put("facet", aggregated_facets);

    JSONObject all_cells_facet = new JSONObject();
    all_cells_facet.put("all_cells", all_cells_facet_child);

    JSONObject all_sites_facet = new JSONObject();
    all_sites_facet.put("type", "terms");
    all_sites_facet.put("field", "site_id_s");
    all_sites_facet.put("limit", -1);
    all_sites_facet.put("facet", all_cells_facet);

    jsonFacetObject.put("all_sites", all_sites_facet);

    return jsonFacetObject.toJSONString();
  }

  public String buildJsonFacetForSiteStats(String id, List<String> requestedIndicators) {
    JSONObject jsonFacetObject = new JSONObject();
    JSONObject aggregated_facets = new JSONObject();

    if (requestedIndicators.contains(TelcoPlanningConstants.Indicators.USER_TXN_CHILD_UNIQUE_PEOPLE)
        || requestedIndicators
            .contains(TelcoPlanningConstants.Indicators.USER_TXN_CHILD_AVG_STAY_DURATION)) {

      JSONObject blockParent = new JSONObject();
      blockParent.put("blockParent", id + ":*");

      JSONObject hllId = new JSONObject();
      hllId.put("unique_count", "hll(" + id + ")");

      JSONObject unique_count_facet_child = new JSONObject();
      unique_count_facet_child.put("type", "query");
      unique_count_facet_child.put("q", "*:*");
      unique_count_facet_child.put("domain", blockParent);
      unique_count_facet_child.put("facet", hllId);
      aggregated_facets.put("unique_count", unique_count_facet_child);
    }

    aggregated_facets.putAll(addUserTxnIndicatorFacets(requestedIndicators));

    JSONObject all_sites_facet = new JSONObject();
    all_sites_facet.put("type", "terms");
    all_sites_facet.put("field", "site_id_s");
    all_sites_facet.put("limit", -1);
    all_sites_facet.put("facet", aggregated_facets);

    jsonFacetObject.put("all_sites", all_sites_facet);

    return jsonFacetObject.toJSONString();
  }

  private JSONObject addUserTxnIndicatorFacets(List<String> requestedIndicators) {
    JSONObject aggFacets = new JSONObject();
    for (String reqIndicator : requestedIndicators) {
      Map<String, Object> indicatorInfo =
          solrToJsonPropSvc.userTxnCalculatedIndicatorsMap.get(reqIndicator);
      if (indicatorInfo != null
          && (Boolean) indicatorInfo.get(TelcoPlanningConstants.SolrToJsonPropMap.IS_INDICATOR)) {
        String formula =
            String.valueOf(indicatorInfo.get(TelcoPlanningConstants.SolrToJsonPropMap.FORMULA));
        String facetName =
            String.valueOf(indicatorInfo.get(TelcoPlanningConstants.SolrToJsonPropMap.FACET_NAME));
        if (!aggFacets.containsKey(facetName)) {
          aggFacets.put(facetName, formula);
        }
      }
    }
    return aggFacets;
  }

}
