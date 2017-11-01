/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.solr.repository;

import com.dataspark.api.data.TopRoiStatsResponse;
import com.dataspark.api.exception.BadRequestException;
import com.dataspark.api.exception.ServiceException;
import com.dataspark.api.service.SolrToJsonPropMappingService;
import com.dataspark.api.solr.parser.RequestQueryParser;
import com.dataspark.api.solr.parser.ResponseParser;
import com.dataspark.api.solr.util.TelcoPlanningSolrUtil;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * @author ragarwal, 2017-03-01
 */
@Service
@Log4j
public class SolrRepositoryImpl implements ISolrRepository {

  @Autowired
  private SolrClient solrClient;

  @Autowired
  private RequestQueryParser requestQueryParser;

  @Autowired
  private ResponseParser responseParser;

  @Autowired
  private TelcoPlanningSolrUtil telcoPlanningSolrUtil;

  @Autowired
  private SolrToJsonPropMappingService solrToJsonPropSvc;

  @Value("${collection.toproistats.prefix}")
  private String topRoiStatsCollectionPrefix;

  @Value("${collection.cell.prefix}")
  private String cellCollectionPrefix;

  @Value("${collection.user.txn.prefix}")
  private String userTxnCollectionPrefix;

  public static SimpleDateFormat sdf = new SimpleDateFormat("YYYYMM");

  @Override
  public TopRoiStatsResponse getTopRoiStats(String reqYearMonth)
      throws SolrServerException, IOException {
    log.info("Solr call for getTopRoiStats for requested year and month: " + reqYearMonth);
    TopRoiStatsResponse topRoiStatsResponseMO = new TopRoiStatsResponse();
    NamedList<?> nameList;
    String collectionName = generateTopRoiStatsCollectionName(reqYearMonth);
    SolrParams sp = requestQueryParser.getTopRoiStatsQuery(collectionName);
    try {
      nameList = runQuery(sp);
    } catch (SolrException e) {
      log.debug(
          "Last month collection not available in solr. Finding the latest top roi collection!!",
          e);
      collectionName =
          telcoPlanningSolrUtil.getLatestCollection(solrClient, topRoiStatsCollectionPrefix);
      if (collectionName.isEmpty()) {
        log.error("Could not find collection.");
        topRoiStatsResponseMO.setTopRoiStats(new ArrayList<Map<String, Object>>());
        topRoiStatsResponseMO.setMeta(
            telcoPlanningSolrUtil.buildTopRoiStatsMeta(topRoiStatsCollectionPrefix + reqYearMonth));
        return topRoiStatsResponseMO;
      }
      sp = requestQueryParser.getTopRoiStatsQuery(collectionName);
      nameList = runQuery(sp);
    }
    List<Map<String, Object>> topRoiStatsMOs = responseParser.parseTopRoiStatsResults(nameList);
    topRoiStatsResponseMO.setTopRoiStats(topRoiStatsMOs);
    topRoiStatsResponseMO.setMeta(telcoPlanningSolrUtil.buildTopRoiStatsMeta(collectionName));
    return topRoiStatsResponseMO;
  }

  @Override
  @Cacheable("site_cell_payload_revenue_details")
  public Map<String, Map<String, Object>> getSiteDetails(String month)
      throws SolrServerException, IOException, BadRequestException, ServiceException {

    log.info("Solr call for get static cell details grouped by site from cell collection.");

    // Get the Cell Filters
    Set<String> cellSolrFilters = new HashSet<String>();
    cellSolrFilters.add("-(roi1_id_s:null)");

    log.info("Cell Filters are: " + cellSolrFilters);

    Map<String, Map<String, Object>> siteDetails = null;
    NamedList<?> nameList;
    String collectionName = cellCollectionPrefix + month;
    log.info("Collection name is: " + collectionName);

    // Set the fields that needs to be read
    List<String> fieldList = new ArrayList<String>();
    for (Entry<String, String> cellInfoEntry : solrToJsonPropSvc.cellCollCellInfoMap.entrySet()) {
      String formula = solrToJsonPropSvc.cellCollIndicatorsFormulaMap.get(cellInfoEntry.getValue());
      if (!StringUtils.isEmpty(formula)) {
        // this cell info is a indicator and is made of sum of other solr fields
        String compositeField = cellInfoEntry.getKey().concat(":").concat(formula);
        fieldList.add(compositeField);
      } else {
        fieldList.add(cellInfoEntry.getKey());
      }
    }

    for (String siteInfoSolrField : solrToJsonPropSvc.cellCollSiteInfoMap.keySet()) {
      fieldList.add(siteInfoSolrField);
    }

    SolrParams sp =
        requestQueryParser.getSiteDetailsQuery(collectionName, cellSolrFilters, fieldList);
    try {
      nameList = runQuery(sp);
      siteDetails = responseParser.parseSiteDetailsResults(nameList);
    } catch (SolrException e) {
      log.error("SolrException in get site details: " + e);
      throw new ServiceException("Exception in get site details: " + e.getMessage());
    }
    return siteDetails;
  }

  @Override
  @Cacheable("site_home_work_details")
  public Map<String, Map<String, Object>> getHomeWorkDetails(List<String> indicators,
      String collectionNames, Set<String> parentFilters, Set<String> childFilters)
      throws SolrServerException, IOException, BadRequestException, ServiceException {
    return processHomeWorkDetails(indicators, collectionNames, parentFilters, childFilters);
  }


  @Override
  public List<Map<String, Object>> getCellStats(List<String> indicators, String collectionNames,
      Set<String> parentFilters, Set<String> childFilters)
      throws SolrServerException, IOException, BadRequestException, ServiceException {
    log.info("Solr call to get cell stats - indicators per cell, grouped by site.");

    NamedList<?> nameList;
    List<Map<String, Object>> cellDetailsResponseList = null;
    try {
      SolrParams sp = requestQueryParser.getCellStatsQuery(parentFilters, childFilters,
          collectionNames, indicators);
      nameList = runQuery(sp);
      cellDetailsResponseList = responseParser.parseCellStatsResults(nameList, indicators);
    } catch (SolrException e) {
      log.debug("Error in get cell stats", e);
      throw new ServiceException("Exception in get cell stats: " + e.getMessage());
    }
    return cellDetailsResponseList;
  }


  @Override
  @Cacheable("site_sms_call_ppl_details")
  public List<Object[]> getSiteStats(List<String> indicators, String collectionNames,
      Set<String> parentFilters, Set<String> childFilters)
      throws SolrServerException, IOException, BadRequestException, ServiceException {
    return processSiteStats(indicators, collectionNames, parentFilters, childFilters);
  }


  @Override
  @Cacheable("site_home_work_details_no_filters")
  public Map<String, Map<String, Object>> getHomeWorkDetailsWithoutFilters(List<String> indicators,
      String collectionNames, Set<String> parentFilters, Set<String> childFilters)
      throws SolrServerException, IOException, BadRequestException, ServiceException {
    return processHomeWorkDetails(indicators, collectionNames, parentFilters, childFilters);
  }

  @Override
  @Cacheable("site_sms_call_ppl_details_no_filters")
  public List<Object[]> getSiteStatsWithoutFilters(List<String> indicators, String collectionNames,
      Set<String> parentFilters, Set<String> childFilters)
      throws SolrServerException, IOException, BadRequestException, ServiceException {
    return processSiteStats(indicators, collectionNames, parentFilters, childFilters);
  }

  @Override
  public HashMap<String, String> exportUsersPerSites(String collectionNames,
      Set<String> parentFilters, Set<String> childFilters, String header)
      throws SolrServerException, IOException, BadRequestException, ServiceException {

    log.info("Solr call for export Users Per Sites.");
    log.info("collectionNames: " + collectionNames);
    log.info("parentFilters: " + parentFilters);
    log.info("childFilters: " + childFilters);

    // Create a list of all export user columns
    List<String> fieldList = new ArrayList<>(solrToJsonPropSvc.userTxnCollProfileInfoMap.keySet());
    List<String> lookupFieldList = solrToJsonPropSvc.userProfileLookupProps;

    NamedList<?> nameList;

    try {
      SolrParams sp = requestQueryParser.exportUsersPerSitesQuery(parentFilters, childFilters,
          collectionNames, fieldList);
      nameList = runQuery(sp);
      HashMap<String, String> exportedUsers =
          responseParser.parseExportUserResults(nameList, fieldList, lookupFieldList, header);
      return exportedUsers;
    } catch (SolrException e) {
      log.debug("Error in exportUsersPerSites", e);
      throw new ServiceException(e.getMessage());
    }
  }

  private NamedList<?> runQuery(SolrParams sp) throws SolrServerException, IOException {
    QueryResponse qr = solrClient.query(sp, METHOD.POST);
    NamedList<?> nameList = qr.getResponse();
    return nameList;
  }

  private String generateTopRoiStatsCollectionName(String reqYearMonth) {
    String collectionSuffix;
    if (!StringUtils.isEmpty(reqYearMonth)) {
      collectionSuffix = reqYearMonth;
    } else {
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.MONTH, -1);
      Date d = cal.getTime();
      collectionSuffix = sdf.format(d);
    }
    return topRoiStatsCollectionPrefix + collectionSuffix;
  }

  private List<Object[]> processSiteStats(List<String> indicators, String collectionNames,
      Set<String> parentFilters, Set<String> childFilters)
      throws ServiceException, SolrServerException, IOException {
    log.info("Solr call for get Site Details info.");
    log.info("indicators: " + indicators);
    log.info("collectionNames: " + collectionNames);
    log.info("parentFilters: " + parentFilters);
    log.info("childFilters: " + childFilters);

    NamedList<?> nameList;
    List<Object[]> siteList = null;
    try {
      SolrParams sp = requestQueryParser.getSiteStatsDetailsQuery(parentFilters, childFilters,
          collectionNames, indicators);
      nameList = runQuery(sp);
      siteList = responseParser.parseSiteStatsResults(nameList, indicators);
    } catch (SolrException e) {
      log.debug("Error in getSiteStats", e);
      throw new ServiceException("Exception in get Site Stats Solr Query: " + e.getMessage());
    }
    return siteList;
  }

  private Map<String, Map<String, Object>> processHomeWorkDetails(List<String> indicators,
      String collectionNames, Set<String> parentFilters, Set<String> childFilters)
      throws ServiceException, SolrServerException, IOException {
    log.info("Solr call for get home work info.");
    log.info("indicators: " + indicators);
    log.info("collectionNames: " + collectionNames);
    log.info("parentFilters: " + parentFilters);
    log.info("childFilters: " + childFilters);

    Map<String, Map<String, Object>> siteCountMap = new HashMap<String, Map<String, Object>>();
    NamedList<?> nameList;

    try {
      SolrParams sp = requestQueryParser.getHomeWorkDetailsQuery(parentFilters, childFilters,
          collectionNames, indicators);
      nameList = runQuery(sp);
      siteCountMap = responseParser.parseHomeWorkResults(nameList);
    } catch (SolrException e) {
      log.debug("Error in getHomeWorkDetails", e);
      throw new ServiceException("Exception in Home Work Solr Query: " + e.getMessage());
    }

    log.info("Returning home work MO count: " + siteCountMap.size());
    return siteCountMap;
  }

  @Override
  public List<Integer> getAvailableCellCollectionMonthsInSolr()
      throws SolrServerException, IOException {

    String collectionPrefix = userTxnCollectionPrefix;
    List<Integer> yearMonths = new ArrayList<Integer>();
    final List<String> collections = telcoPlanningSolrUtil.getAllSolrCollections(solrClient);
    for (String collection : collections) {
      if (collection.startsWith(collectionPrefix)) {
        String dateValue = collection.substring(collectionPrefix.length());
        String yearMonth = dateValue.substring(0, 6);
        int yearMonthInt = Integer.parseInt(yearMonth);
        yearMonths.add(yearMonthInt);
      }
    }
    return yearMonths;
  }

}
