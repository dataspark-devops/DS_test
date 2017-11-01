/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.solr.parser;

import com.dataspark.api.data.TelcoPlanningConstants;
import com.dataspark.api.service.FilterService;
import com.dataspark.api.service.SolrToJsonPropMappingService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.extern.log4j.Log4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author ragarwal, 2017-03-01
 */
@Service
@Log4j
@SuppressWarnings("rawtypes")
public class ResponseParser {

  @Autowired
  private FilterService filterService;

  @Autowired
  private SolrToJsonPropMappingService solrToJsonPropSvc;

  public List<Map<String, Object>> parseTopRoiStatsResults(NamedList nameList) {
    List<Map<String, Object>> topRoiStats = new ArrayList<Map<String, Object>>();
    SolrDocumentList docs =
        (SolrDocumentList) nameList.get(TelcoPlanningConstants.OutputMapField.RESPONSE);
    log.info("Results count: " + docs.getNumFound());
    for (SolrDocument doc : docs) {
      Map<String, Object> roiStats = new HashMap<String, Object>();
      for (Entry<String, String> entry : solrToJsonPropSvc.roiStatsCollInfoMap.entrySet()) {
        roiStats.put(entry.getValue(), doc.getFieldValue(entry.getKey()));
      }
      topRoiStats.add(roiStats);
    }

    return topRoiStats;
  }

  // reads all the static information about site and cell
  public Map<String, Map<String, Object>> parseSiteDetailsResults(NamedList nameList) {
    // Map<site_id (lat_lon), Map<cell_id (cgi), Map<cell_info>>>)
    Map<String, Map<String, Object>> cellCollMap = new HashMap<String, Map<String, Object>>();

    SolrDocumentList docs =
        (SolrDocumentList) nameList.get(TelcoPlanningConstants.OutputMapField.RESPONSE);
    log.info("Results count: " + docs.getNumFound());

    for (SolrDocument doc : docs) {
      String latLon = String.valueOf(doc.getFieldValue(TelcoPlanningConstants.SolrField.LAT_LON));
      Map<String, Object> siteInfo = null;
      Map<String, Object> allCellsPerSite = null;

      // if the map contains the site_id, update the site info
      if (cellCollMap.containsKey(latLon)) {
        siteInfo = cellCollMap.get(latLon);
        allCellsPerSite =
            (Map<String, Object>) siteInfo.get(TelcoPlanningConstants.OutputMapField.CELL_STATS);
      } else {
        siteInfo = new HashMap<>();
        for (Entry<String, String> entry : solrToJsonPropSvc.cellCollSiteInfoMap.entrySet()) {
          siteInfo.put(entry.getValue(), doc.getFieldValue(entry.getKey()));
        }
        allCellsPerSite = new HashMap<String, Object>();
      }

      // populate cell info for the site
      Map<String, Object> cellInfo = new HashMap<String, Object>();
      for (Entry<String, String> entry : solrToJsonPropSvc.cellCollCellInfoMap.entrySet()) {
        cellInfo.put(entry.getValue(), doc.getFieldValue(entry.getKey()));
      }

      String cellId = String.valueOf(doc.getFieldValue(TelcoPlanningConstants.SolrField.CGI));
      allCellsPerSite.put(cellId, cellInfo);
      siteInfo.put(TelcoPlanningConstants.OutputMapField.CELL_STATS, allCellsPerSite);
      cellCollMap.put(latLon, siteInfo);
    }

    return cellCollMap;
  }


  public HashMap<String, String> parseExportUserResults(NamedList nameList, List<String> fieldList,
      List<String> exportUsersLookupFields, String header) throws IOException {
    HashMap<String, String> response = new HashMap<String, String>();
    StringBuilder exportedUsers = new StringBuilder();

    SolrDocumentList docs = (SolrDocumentList) nameList.get("response");
    log.info("Results count: " + docs.getNumFound());

    // write header in the file
    exportedUsers.append(header.replace("%userscount%", String.valueOf(docs.getNumFound())));
    exportedUsers.append(TelcoPlanningConstants.NEWLINE);
    for (SolrDocument doc : docs) {
      Object[] userProfile = new Object[fieldList.size()];
      for (String solrField : fieldList) {
        String solrValue = String.valueOf(doc.getFieldValue(solrField));
        if (exportUsersLookupFields.contains(solrField) && solrValue != null
            && !StringUtils.isEmpty(solrValue) && solrValue != "null") {
          String uiValue = filterService.inverseValueMap.get(solrField).get(solrValue);
          String[] values = uiValue.split(TelcoPlanningConstants.Delimiters.DOT);
          solrValue = values[values.length - 1];
        }
        userProfile[solrToJsonPropSvc.userProfileJsonPropIndexMap
            .get(solrToJsonPropSvc.userTxnCollProfileInfoMap.get(solrField))] = solrValue;
      }
      exportedUsers.append(StringUtils.join(userProfile, TelcoPlanningConstants.Delimiters.PIPE));
      exportedUsers.append(TelcoPlanningConstants.NEWLINE);
    }
    response.put(TelcoPlanningConstants.EXPORTED_USERS_RESULTS, exportedUsers.toString());
    return response;
  }

  @SuppressWarnings("unchecked")
  public Map<String, Map<String, Object>> parseHomeWorkResults(NamedList<?> nameList) {
    Map<String, Map<String, Object>> siteCountMap = new HashMap<String, Map<String, Object>>();

    SimpleOrderedMap map = (SimpleOrderedMap) nameList.get("facets");
    List<SimpleOrderedMap> homeSites =
        (List<SimpleOrderedMap>) map.findRecursive("home_sites", "buckets");
    List<SimpleOrderedMap> workSites =
        (List<SimpleOrderedMap>) map.findRecursive("work_sites", "buckets");

    if (!CollectionUtils.isEmpty(homeSites)) {
      for (SimpleOrderedMap homeSite : homeSites) {
        String site = String.valueOf(homeSite.get("val"));
        String count = String.valueOf(homeSite.get("count"));
        Map<String, Object> hwCount = new HashMap<>();
        hwCount.put(TelcoPlanningConstants.Indicators.USER_TXN_PARENT_HOME, Integer.valueOf(count));
        hwCount.put(TelcoPlanningConstants.Indicators.USER_TXN_PARENT_WORK, 0);
        siteCountMap.put(site, hwCount);
      }
    }

    if (!CollectionUtils.isEmpty(workSites)) {
      for (SimpleOrderedMap workSite : workSites) {
        String site = String.valueOf(workSite.get("val"));
        String count = String.valueOf(workSite.get("count"));
        Map<String, Object> hwCount = new HashMap<>();
        if (siteCountMap.containsKey(site)) {
          hwCount = siteCountMap.get(site);
        } else {
          hwCount.put(TelcoPlanningConstants.Indicators.USER_TXN_PARENT_HOME, 0);
        }
        hwCount.put(TelcoPlanningConstants.Indicators.USER_TXN_PARENT_WORK, Integer.valueOf(count));
        siteCountMap.put(site, hwCount);
      }
    }

    return siteCountMap;
  }


  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> parseCellStatsResults(NamedList<?> nameList,
      List<String> indicators) {
    List<Map<String, Object>> cellStatsResponse = new ArrayList<Map<String, Object>>();

    SimpleOrderedMap map = (SimpleOrderedMap) nameList.get("facets");
    List<SimpleOrderedMap> allSites =
        (List<SimpleOrderedMap>) map.findRecursive("all_sites", "buckets");

    if (!CollectionUtils.isEmpty(allSites)) {
      for (SimpleOrderedMap site : allSites) {

        Map<String, Object> siteInfo = new HashMap<String, Object>();
        // lat_lon_s
        String siteId = String.valueOf(site.get("val"));
        siteInfo.put(TelcoPlanningConstants.OutputMapField.SITE_ID, siteId);
        List<SimpleOrderedMap> allCells =
            (List<SimpleOrderedMap>) site.findRecursive("all_cells", "buckets");

        List<Map<String, Object>> allCellsPerSite = new ArrayList<Map<String, Object>>();
        for (SimpleOrderedMap cell : allCells) {
          Map<String, Object> cellInfo = new HashMap<String, Object>();
          Long uniqueCountValue = 0l;
          Double totalStayValue = 0d;
          cellInfo.put(TelcoPlanningConstants.OutputMapField.CELL_ID,
              (String.valueOf(cell.get("val"))));

          // populate the requested calculated indicators
          for (Entry<String, Map<String, Object>> entry : solrToJsonPropSvc.userTxnCalculatedIndicatorsMap
              .entrySet()) {
            String facetName = String
                .valueOf(entry.getValue().get(TelcoPlanningConstants.SolrToJsonPropMap.FACET_NAME));
            if (indicators.contains(entry.getKey())) {
              cellInfo.put(entry.getKey(), (Double) (cell.get(facetName)));
            }
          }

          if (cell.get("unique_count") != null) {
            SimpleOrderedMap uniqueCount = (SimpleOrderedMap) cell.get("unique_count");
            if (uniqueCount != null) {
              uniqueCountValue = Long.valueOf(String.valueOf(uniqueCount.get("unique_count")));
              if (indicators
                  .contains(TelcoPlanningConstants.Indicators.USER_TXN_CHILD_UNIQUE_PEOPLE)) {
                cellInfo.put(TelcoPlanningConstants.Indicators.USER_TXN_CHILD_UNIQUE_PEOPLE,
                    uniqueCountValue);
              }
            }
          }
          if (cell.get("total_stay_duration") != null) {
            totalStayValue = ((Double) cell.get("total_stay_duration")) / 60;
            if (indicators
                .contains(TelcoPlanningConstants.Indicators.USER_TXN_CHILD_TOTAL_STAY_DURATION)) {
              cellInfo.put(TelcoPlanningConstants.Indicators.USER_TXN_CHILD_TOTAL_STAY_DURATION,
                  Double.valueOf(String.format("%.1f", totalStayValue)));
            }
          }
          if (indicators
              .contains(TelcoPlanningConstants.Indicators.USER_TXN_CHILD_AVG_STAY_DURATION)) {
            cellInfo.put(TelcoPlanningConstants.Indicators.USER_TXN_CHILD_AVG_STAY_DURATION,
                Double.valueOf(String.format("%.1f", (totalStayValue / uniqueCountValue))));
          }
          allCellsPerSite.add(cellInfo);
        }
        siteInfo.put(TelcoPlanningConstants.OutputMapField.CELL_STATS, allCellsPerSite);
        cellStatsResponse.add(siteInfo);
      }
    }

    return cellStatsResponse;
  }


  @SuppressWarnings("unchecked")
  public List<Object[]> parseSiteStatsResults(NamedList<?> nameList, List<String> indicators) {

    List<Object[]> siteList = new ArrayList<Object[]>();

    SimpleOrderedMap map = (SimpleOrderedMap) nameList.get("facets");
    List<SimpleOrderedMap> allSites =
        (List<SimpleOrderedMap>) map.findRecursive("all_sites", "buckets");

    if (!CollectionUtils.isEmpty(allSites)) {
      for (SimpleOrderedMap allSite : allSites) {
        Object[] obj = new Object[solrToJsonPropSvc.siteInfoJsonPropIndexMap.size()];
        Long uniqueCountValue = 0l;
        Double totalStayValue = 0d;
        // lat_lon_s
        String siteId = String.valueOf(allSite.get("val"));
        obj[solrToJsonPropSvc.siteInfoJsonPropIndexMap
            .get(TelcoPlanningConstants.OutputMapField.ID)] = siteId;

        String latLonArr[] = siteId.split(TelcoPlanningConstants.Delimiters.UNDERSCORE);
        obj[solrToJsonPropSvc.siteInfoJsonPropIndexMap
            .get(TelcoPlanningConstants.OutputMapField.LATITUDE)] = Double.valueOf(latLonArr[0]);
        obj[solrToJsonPropSvc.siteInfoJsonPropIndexMap
            .get(TelcoPlanningConstants.OutputMapField.LONGITUDE)] = Double.valueOf(latLonArr[1]);

        // populate the requested calculated indicators
        for (Entry<String, Map<String, Object>> entry : solrToJsonPropSvc.userTxnCalculatedIndicatorsMap
            .entrySet()) {
          String facetName = String
              .valueOf(entry.getValue().get(TelcoPlanningConstants.SolrToJsonPropMap.FACET_NAME));
          if (indicators.contains(entry.getKey())) {
            obj[solrToJsonPropSvc.siteInfoJsonPropIndexMap.get(entry.getKey())] =
                (Double) (allSite.get(facetName));
          }
        }

        if (allSite.get("unique_count") != null) {
          SimpleOrderedMap uniqueCount = (SimpleOrderedMap) allSite.get("unique_count");
          if (uniqueCount != null) {
            uniqueCountValue = Long.valueOf(String.valueOf(uniqueCount.get("unique_count")));
            if (indicators
                .contains(TelcoPlanningConstants.Indicators.USER_TXN_CHILD_UNIQUE_PEOPLE)) {
              obj[solrToJsonPropSvc.siteInfoJsonPropIndexMap
                  .get(TelcoPlanningConstants.Indicators.USER_TXN_CHILD_UNIQUE_PEOPLE)] =
                      uniqueCountValue;
            }
          }
        }
        if (allSite
            .get(TelcoPlanningConstants.Indicators.USER_TXN_CHILD_TOTAL_STAY_DURATION) != null) {
          totalStayValue = ((Double) (allSite
              .get(TelcoPlanningConstants.Indicators.USER_TXN_CHILD_TOTAL_STAY_DURATION))) / 60;
          if (indicators
              .contains(TelcoPlanningConstants.Indicators.USER_TXN_CHILD_TOTAL_STAY_DURATION)) {
            obj[solrToJsonPropSvc.siteInfoJsonPropIndexMap
                .get(TelcoPlanningConstants.Indicators.USER_TXN_CHILD_TOTAL_STAY_DURATION)] =
                    Double.valueOf(String.format("%.1f", totalStayValue));
          }
        }
        if (indicators
            .contains(TelcoPlanningConstants.Indicators.USER_TXN_CHILD_AVG_STAY_DURATION)) {
          obj[solrToJsonPropSvc.siteInfoJsonPropIndexMap
              .get(TelcoPlanningConstants.Indicators.USER_TXN_CHILD_AVG_STAY_DURATION)] =
                  Double.valueOf(String.format("%.1f", (totalStayValue / uniqueCountValue)));
        }
        siteList.add(obj);
      }
    }

    return siteList;
  }


}
