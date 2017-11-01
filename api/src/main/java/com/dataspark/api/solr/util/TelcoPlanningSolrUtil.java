/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.solr.util;

import com.dataspark.api.data.CellDetailsRequest;
import com.dataspark.api.data.TelcoPlanningConstants;
import com.dataspark.api.data.TopRoiStatsMeta;
import com.dataspark.api.exception.BadRequestException;
import com.dataspark.api.service.FilterService;
import com.dataspark.api.service.SolrToJsonPropMappingService;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import lombok.extern.log4j.Log4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.common.util.NamedList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author ragarwal
 *
 *         2017-03-01
 */
@Log4j
@Service
public class TelcoPlanningSolrUtil {

  @Value("${collection.user.txn.prefix}")
  private String userTxnCollectionPrefix;

  @Autowired
  private FilterService filterService;

  @Autowired
  private SolrToJsonPropMappingService solrToJsonPropSvc;

  public String getLatestCollection(SolrClient solrClient, String collectionPrefix)
      throws SolrServerException, IOException {
    int largest = 0;
    String latestCollection = "";
    final List<String> collections = getAllSolrCollections(solrClient);
    for (String collection : collections) {
      if (collection.startsWith(collectionPrefix)) {
        String dateValue = collection.substring(collectionPrefix.length());
        String year = dateValue.substring(0, 4);
        String month = dateValue.substring(4);
        int collectionDateValue = Integer.parseInt(year + month);
        if (collectionDateValue > largest) {
          largest = collectionDateValue;
          latestCollection = collection;
        }
      }
    }
    log.debug("Latest Collection: " + latestCollection);
    return latestCollection;
  }

  public List<String> getAllSolrCollections(SolrClient solrClient)
      throws SolrServerException, IOException {
    SolrRequest listReq = new CollectionAdminRequest.List();
    NamedList<Object> qr = solrClient.request(listReq);
    List<String> collections = (List<String>) qr.get("collections");
    return collections;
  }

  public String getOrFilterFromList(List<String> filterList) {
    List<String> filterListModified = new ArrayList<String>();
    for (String s : filterList) {
      if (s.contains(", ")) {
        filterListModified.add("\"" + s + "\"");
      } else {
        filterListModified.add(s);
      }
    }
    String filter = StringUtils.join(filterListModified, " OR ");
    return "(" + filter + ")";
  }

  public String getRangeFilterFromList(List<String> filterList) {
    String filter = StringUtils.join(filterList, " TO ");
    return "[" + filter + "]";
  }


  private String getSolrFilter(String key, List<String> values) throws BadRequestException {
    String mappedKey = filterService.fieldMap.get(key);
    if (mappedKey == null) {
      throw new BadRequestException("Filter Key [" + key + "] is invalid");
    }

    String valueString = "";
    Map<String, String> mapValues = filterService.valueMap.get(key);

    List<String> valueList = values;
    if (!mapValues.containsKey(TelcoPlanningConstants.MATCHALL_REGEX) || !mapValues
        .get(TelcoPlanningConstants.MATCHALL_REGEX).equals(TelcoPlanningConstants.MATCHALL_REGEX)) {
      valueList = new ArrayList<String>();
      for (String value : values) {
        valueList.add(mapValues.get(value));
      }
    }
    if (filterService.typeMap.get(key) != null
        && filterService.typeMap.get(key).equalsIgnoreCase(TelcoPlanningConstants.RANGE)) {
      valueString = getRangeFilterFromList(valueList);
    } else {
      valueString = getOrFilterFromList(valueList);
    }
    return mappedKey + ":" + valueString;
  }

  public String getCellCollectionName(Set<String> months) {
    List<String> monthList = new ArrayList<String>(months);
    Collections.sort(monthList);
    return (String) monthList.get(monthList.size() - 1);
  }

  public String getUserTxnCollections(Set<String> yearMonths) {
    List<String> collectionNames = new ArrayList<String>();
    for (String yearMonth : yearMonths) {
      collectionNames.add(userTxnCollectionPrefix + yearMonth);
    }

    return StringUtils.join(collectionNames, ",");
  }

  public Set<String> getUserTxnFilters(CellDetailsRequest cellDetailsRequest)
      throws BadRequestException {

    Set<String> userTxnFilters = new HashSet<String>();

    if (cellDetailsRequest.getFilterUser() != null) {
      Map<String, List<String>> userFilters = cellDetailsRequest.getFilterUser();
      for (Entry<String, List<String>> entry : userFilters.entrySet()) {
        userTxnFilters.add(getSolrFilter(entry.getKey(), entry.getValue()));
      }
    }

    if (cellDetailsRequest.getFilterDevice() != null) {
      Map<String, List<String>> deviceFilters = cellDetailsRequest.getFilterDevice();
      for (Entry<String, List<String>> entry : deviceFilters.entrySet()) {
        userTxnFilters.add(getSolrFilter(entry.getKey(), entry.getValue()));
      }
    }
    return userTxnFilters;
  }

  public Set<String> getChildFilters(CellDetailsRequest cellDetailsRequest)
      throws BadRequestException, ParseException {

    Set<String> childFilters = getCellFilters(cellDetailsRequest);
    if (cellDetailsRequest.getPeriodType().equalsIgnoreCase(TelcoPlanningConstants.WEEK)) {
      Set<Integer> weeks = DateUtils.getWeekNumbers(cellDetailsRequest.getPeriodRange());
      log.info("Weeks are: " + weeks);
      String weekFilter = "(" + StringUtils.join(weeks, " OR ") + ")";
      childFilters.add(TelcoPlanningConstants.SolrField.WEEK + ":" + weekFilter);
    }

    // add sites filters
    if (!CollectionUtils.isEmpty(cellDetailsRequest.getSites())) {
      List<String> sites = new ArrayList<String>();
      for (String site : cellDetailsRequest.getSites()) {
        sites.add("\"".concat(site).concat("\""));
      }
      childFilters.add(TelcoPlanningConstants.SolrField.SITE_ID + ":" + getOrFilterFromList(sites));
    }

    return childFilters;
  }

  public Set<String> getCellFilters(CellDetailsRequest cellDetailsRequest)
      throws BadRequestException {

    Set<String> cellSolrFilters = new HashSet<String>();
    for (Entry<String, List<String>> roiFilter : cellDetailsRequest.getRoiFilters().entrySet()) {
      cellSolrFilters.add(getSolrFilter(roiFilter.getKey(), roiFilter.getValue()));
    }

    if (cellDetailsRequest.getFilterCell() != null) {
      Map<String, List<String>> cellFilters = cellDetailsRequest.getFilterCell();
      for (Entry<String, List<String>> entry : cellFilters.entrySet()) {
        cellSolrFilters.add(getSolrFilter(entry.getKey(), entry.getValue()));
      }
    }

    return cellSolrFilters;
  }

  public TopRoiStatsMeta buildTopRoiStatsMeta(String collectionName) {
    String yearMonth = (collectionName.split(TelcoPlanningConstants.Delimiters.UNDERSCORE))[3];
    return new TopRoiStatsMeta(yearMonth.substring(4), yearMonth.substring(0, 4),
        buildTopRoiStatsIndicators());
  }

  private List<Map<String, String>> buildTopRoiStatsIndicators() {
    List<Map<String, String>> indicators = new ArrayList<Map<String, String>>();
    for (Entry<String, String> indicator : solrToJsonPropSvc.roiStatsJsonPropHeaderMap
        .entrySet()) {
      Map<String, String> indicatorMap = new HashMap<String, String>();
      indicatorMap.put(TelcoPlanningConstants.KEY, indicator.getKey());
      indicatorMap.put(TelcoPlanningConstants.LABEL, indicator.getValue());
      indicators.add(indicatorMap);
    }

    return indicators;
  }
}
