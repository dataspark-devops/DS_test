/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.service;

import com.dataspark.api.data.CellDetailsRequest;
import com.dataspark.api.data.CellDetailsResponse;
import com.dataspark.api.data.Response;
import com.dataspark.api.data.RoiLevelInfo;
import com.dataspark.api.data.RoiNode;
import com.dataspark.api.data.TelcoPlanningConstants;
import com.dataspark.api.data.TopRoiStatsResponse;
import com.dataspark.api.exception.BadRequestException;
import com.dataspark.api.exception.ServiceException;
import com.dataspark.api.solr.repository.ISolrRepository;
import com.dataspark.api.solr.util.DateUtils;
import com.dataspark.api.solr.util.GeoJsonGenerator;
import com.dataspark.api.solr.util.TelcoPlanningSolrUtil;
import com.google.common.base.Preconditions;
import com.jayway.jsonpath.JsonPath;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.log4j.Log4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * @author ragarwal,ashutosh 2017-03-01
 */
@Service("telcoPlanningService")
@Log4j
public class TelcoPlanningServiceImpl implements ITelcoPlanningService {

  @Autowired
  private ISolrRepository solrRepository;

  @Autowired
  private TelcoPlanningSolrUtil telcoPlanningSolrUtil;

  @Autowired
  private GeoJsonGenerator geoJsonGenerator;

  @Autowired
  private TelcoPlanningServiceHelper telcoPlanningServiceHelper;

  @Value("${roi.level.keys}")
  private String roiLevels;

  @Value("${telcoplanning.geojson.file}")
  private String geoJsonFile;

  @Value("${auth.geoHierarchy.endpoint.url}")
  private String geoHierarchyEndPoint;

  @Value("${auth.settings.endpoint.url}")
  private String settingsEndPoint;

  @Value("${auth.enabled}")
  private boolean authEnabled;

  @Override
  public TopRoiStatsResponse getTopRoiStats(String reqYearMonth, String userName)
      throws ServiceException {
    TopRoiStatsResponse topRoiStatsResponse = null;
    List<Map<String, Object>> roiStats = null;
    try {
      topRoiStatsResponse = solrRepository.getTopRoiStats(reqYearMonth);
      if (authEnabled && !CollectionUtils.isEmpty(topRoiStatsResponse.getTopRoiStats())) {
        roiStats = telcoPlanningServiceHelper.getFilteredTopRoiStats(userName,
            topRoiStatsResponse.getTopRoiStats());
        topRoiStatsResponse.setTopRoiStats(roiStats);
      }
    } catch (Exception e) {
      log.error("Exception in getTopRoiStats.", e);
      throw new ServiceException("Failed to get results from data store." + e.getMessage());
    }
    return topRoiStatsResponse;
  }

  @Override
  public CellDetailsResponse getCellDetailsBySites(String userName,
      CellDetailsRequest cellDetailsRequest) throws Exception {
    CellDetailsResponse cellDetailsResponse = new CellDetailsResponse();
    List<Map<String, Object>> cellDetails = new ArrayList<Map<String, Object>>();

    // Validate the Request Parameters including Filters
    telcoPlanningServiceHelper.validateRequestParameters(cellDetailsRequest, true, true);

    // Authorize request in authsquare if auth is enabled
    if (authEnabled) {
      telcoPlanningServiceHelper.authorizeRequest(userName, cellDetailsRequest);
    }

    // Get the months list where the results are needed
    Set<String> months = DateUtils.getYearMonthList(cellDetailsRequest.getPeriodRange());
    log.info("Months are: " + months);

    // Get user txn collections from months
    String collectionNames = telcoPlanningSolrUtil.getUserTxnCollections(months);

    // Get user txn parent and child filters
    Set<String> parentFilters = telcoPlanningSolrUtil.getUserTxnFilters(cellDetailsRequest);
    Set<String> childFilters = telcoPlanningSolrUtil.getChildFilters(cellDetailsRequest);

    Map<String, Map<String, Object>> homeWorkMap = null;
    List<Map<String, Object>> cellStatsPerSite = null;
    Map<String, Map<String, Object>> siteDetailsMap = null;

    try {
      List<String> requestedIndicators = cellDetailsRequest.getIndicators();

      // Solr Call for Home and Work Count
      homeWorkMap = !(CollectionUtils.intersection(requestedIndicators,
          TelcoPlanningConstants.Indicators.getUserTxnParentIndicators()).isEmpty())
              ? solrRepository.getHomeWorkDetails(cellDetailsRequest.getIndicators(),
                  collectionNames, parentFilters, childFilters)
              : null;

      if (homeWorkMap != null) {
        log.info("Cells API: Received homeWorkMap with size: " + homeWorkMap.size());
      } else {
        log.info("Cells API: Received homeWorkMap with size: 0");
      }

      // Solr call for SMS count, Calls count and Unique ppl count, stay duration per cell grouped
      // by site
      cellStatsPerSite = solrRepository.getCellStats(cellDetailsRequest.getIndicators(),
          collectionNames, parentFilters, childFilters);
      log.info("Cells API: Received cellResponse List with size: " + cellStatsPerSite.size());


      // Solr call for static site details [Site name, ROIS, LAT LON] and cell details [revenue and
      // payload]
      String latestMonth = telcoPlanningSolrUtil.getCellCollectionName(months);
      siteDetailsMap = solrRepository.getSiteDetails(latestMonth);
      log.info("Cells API: Received siteDetailsMap with size: " + siteDetailsMap.size());

      cellDetails = telcoPlanningServiceHelper.buildCellDetailsReponse(homeWorkMap,
          cellStatsPerSite, siteDetailsMap, cellDetailsRequest.getIndicators(),
          cellDetailsRequest.getPeriodType());

      cellDetailsResponse.setMeta(cellDetailsRequest);
      cellDetailsResponse.setResults(cellDetails);
    } catch (SolrServerException | IOException e) {
      log.error("Error in getting getCellDetailsBySites.", e);
      throw new ServiceException("Failed to get results from data store.");
    }
    return cellDetailsResponse;
  }

  @Override
  public Response getSiteDetailsByROI(String userName, CellDetailsRequest cellDetailsRequest)
      throws Exception {
    Response siteDetailsResponse = new Response();
    List<Object[]> siteResponseMOs = new ArrayList<Object[]>();

    // Validate the Request Parameters including Filters
    telcoPlanningServiceHelper.validateRequestParameters(cellDetailsRequest, false, true);

    // Authorize request in authsquare if auth is enabled
    if (authEnabled) {
      telcoPlanningServiceHelper.authorizeRequest(userName, cellDetailsRequest);
    }

    // Get the months list where the results are needed
    Set<String> months = DateUtils.getYearMonthList(cellDetailsRequest.getPeriodRange());
    log.info("Months are: " + months);

    // Get user txn collections from months
    String collectionNames = telcoPlanningSolrUtil.getUserTxnCollections(months);

    // Get user txn parent and child filters
    Set<String> parentFilters = telcoPlanningSolrUtil.getUserTxnFilters(cellDetailsRequest);
    Set<String> childFilters = telcoPlanningSolrUtil.getChildFilters(cellDetailsRequest);

    Map<String, Map<String, Object>> homeWorkMap = null;
    List<Object[]> siteResponseList = null;
    Map<String, Map<String, Object>> siteDetailsMap = null;

    try {
      List<String> requestedIndicators = cellDetailsRequest.getIndicators();

      if ((TelcoPlanningConstants.PeriodType.MONTH.equals(cellDetailsRequest.getPeriodType()))
          && (cellDetailsRequest.getFilterCell() == null
              || cellDetailsRequest.getFilterCell().isEmpty())
          && (cellDetailsRequest.getFilterDevice() == null
              || cellDetailsRequest.getFilterDevice().isEmpty())
          && (cellDetailsRequest.getFilterUser() == null
              || cellDetailsRequest.getFilterUser().isEmpty())) {
        log.info("Get site details by ROI with no filters.");
        homeWorkMap = solrRepository.getHomeWorkDetailsWithoutFilters(
            cellDetailsRequest.getIndicators(), collectionNames, parentFilters, childFilters);
        siteResponseList = solrRepository.getSiteStatsWithoutFilters(
            cellDetailsRequest.getIndicators(), collectionNames, parentFilters, childFilters);
      } else {
        // Solr Call for Home and Work Count
        homeWorkMap = !(CollectionUtils.intersection(requestedIndicators,
            TelcoPlanningConstants.Indicators.getUserTxnParentIndicators()).isEmpty())
                ? solrRepository.getHomeWorkDetails(cellDetailsRequest.getIndicators(),
                    collectionNames, parentFilters, childFilters)
                : null;

        if (homeWorkMap != null) {
          log.info("Sites API: Received homeWorkMap with size: " + homeWorkMap.size());
        } else {
          log.info("Sites API: Received homeWorkMap with size: 0");
        }

        // Solr call for SMS count, Calls count and Unique ppl count per site id=lat_lon_s
        siteResponseList = solrRepository.getSiteStats(cellDetailsRequest.getIndicators(),
            collectionNames, parentFilters, childFilters);
        log.info("Sites API: Received site Response List with size: " + siteResponseList.size());
      }

      // Solr call for Site details [Site name, ROIS, LAT LON] and cell details [revenue and
      // payload]
      String latestMonth = telcoPlanningSolrUtil.getCellCollectionName(months);
      siteDetailsMap = solrRepository.getSiteDetails(latestMonth);
      log.info("Sites API: Received siteDetailsMap with size: " + siteDetailsMap.size());

      // Build the response as array of objects
      siteResponseMOs = telcoPlanningServiceHelper.buildSiteDetailsResponse(homeWorkMap,
          siteResponseList, siteDetailsMap, cellDetailsRequest.getIndicators(),
          cellDetailsRequest.getPeriodType());

      siteDetailsResponse
          .setMeta(telcoPlanningServiceHelper.buildSiteDetailsResponseMeta(cellDetailsRequest));
      siteDetailsResponse.setResults(siteResponseMOs);

    } catch (SolrServerException | IOException e) {
      log.error("Error in getting getSiteDetailsByROI.", e);
      throw new ServiceException("Failed to get results from data store.");
    }
    return siteDetailsResponse;
  }

  @Override
  public List<RoiLevelInfo> getGeoHierarchy(String inputRoiId, String inputRoiLevel,
      String outputRoiLevel) throws ServiceException, BadRequestException {
    try {
      if (StringUtils.isEmpty(inputRoiId) || StringUtils.isEmpty(inputRoiLevel)
          || StringUtils.isEmpty(outputRoiLevel)) {
        throw new BadRequestException("Input parameters are null or empty.");
      }

      LinkedHashMap<String, String> roiLevelsMap = new LinkedHashMap<String, String>();
      for (String roiLevel : roiLevels.split(TelcoPlanningConstants.Delimiters.COMMA)) {
        String keyValue[] = roiLevel.split(TelcoPlanningConstants.Delimiters.COLON);
        roiLevelsMap.put(keyValue[0], keyValue[1]);
      }

      Set<String> roiLevelKeys = roiLevelsMap.keySet();

      if (!roiLevelKeys.contains(inputRoiLevel) || !roiLevelKeys.contains(outputRoiLevel)) {
        throw new BadRequestException("Invalid input and/or output ROI levels.");
      }

      List<RoiLevelInfo> roiInfoMOs = new ArrayList<RoiLevelInfo>();
      File file = new File(geoJsonFile);
      String treeJson = FileUtils.readFileToString(file, "UTF-8");
      String query = telcoPlanningServiceHelper.buildQuery(inputRoiId, inputRoiLevel,
          outputRoiLevel, roiLevelsMap);
      roiInfoMOs = JsonPath.parse(treeJson).read(query, roiInfoMOs.getClass());

      return roiInfoMOs;
    } catch (BadRequestException e) {
      log.error("BadRequestException in getGeoHierarchy: ", e);
      throw e;
    } catch (Exception e) {
      log.error("Error in getting getGeoHierarchy.", e);
      throw new ServiceException("Failed to get results from data store." + e.getMessage());
    }
  }

  @Override
  public String getSiteDetails(String month) throws ServiceException {
    // Auto Warm Site details for a month
    Preconditions.checkArgument(month.length() == 6, "month field is invalid - format : yyyymm");
    Map<String, Map<String, Object>> resp = null;
    try {
      Integer.valueOf(month);
      resp = solrRepository.getSiteDetails(month);
    } catch (Exception e) {
      log.error("Exception in getSiteDetails", e);
      throw new ServiceException("Exception in getSiteDetails: " + e.getMessage());
    }
    if (resp != null) {
      return "SUCCESS";
    } else {
      return "FAILURE";
    }

  }

  @Override
  public String autoWarmSitesAPIForTopROI(String month, String roiId)
      throws SolrServerException, IOException, BadRequestException, ServiceException {

    Preconditions.checkArgument(month.length() == 6, "month field is invalid - format : yyyymm");
    Map<String, Map<String, Object>> hwMap = null;
    List<Object[]> sitesArray = null;
    try {
      Integer.valueOf(month);
      String[] indicators = {"payload", "sms", "revenue", "calls", "home", "work", "unique_people"};
      List<String> indList = Arrays.asList(indicators);
      Set<String> childFilters = new HashSet<String>();
      Set<String> parentFilters = new HashSet<String>();
      childFilters.add("roi1_id_s:(" + roiId + ")");

      Set<String> months = new HashSet<String>();
      months.add(month);
      String collectionNames = telcoPlanningSolrUtil.getUserTxnCollections(months);

      // Cache Solr Call for Home and Work Count
      hwMap = solrRepository.getHomeWorkDetailsWithoutFilters(indList, collectionNames,
          parentFilters, childFilters);

      // Cache Solr call for SMS count, Calls count and Unique ppl count per site
      sitesArray = solrRepository.getSiteStatsWithoutFilters(indList, collectionNames,
          parentFilters, childFilters);

    } catch (Exception e) {
      log.error("Exception in autoWarmSitesAPIForTopROI for ROI - " + roiId, e);
      throw new ServiceException(
          "Exception in autoWarmSitesAPIForTopROI for ROI - " + roiId + e.getMessage());
    }
    if (sitesArray != null && hwMap != null)
      return "SUCCESS";
    else
      return "FAILURE";
  }

  @Override
  public HashMap<String, Object> getModuleSettingsByModuleName(String userName)
      throws SolrServerException, IOException {

    log.debug("Request for getModuleSettingsByModuleName for userName :[" + userName + "]");

    String url = settingsEndPoint;
    ResponseEntity<Object> resp =
        TelcoPlanningServiceHelper.getService(url, Object.class, userName);
    HashMap<String, Object> map = (HashMap<String, Object>) resp.getBody();
    HashMap<String, Object> results =
        (HashMap<String, Object>) map.get(TelcoPlanningConstants.RESULT);

    String timeZoneStr = (String) results.get(TelcoPlanningConstants.TIME_ZONE);
    DateTimeZone tz = DateTimeZone.forID(timeZoneStr);
    List<Map<String, Object>> purchasedPeriods =
        (List<Map<String, Object>>) results.get(TelcoPlanningConstants.PURCHASED_PERIODS);
    List<Integer> months = solrRepository.getAvailableCellCollectionMonthsInSolr();
    Collections.sort(months);

    List<Map<String, String>> availablePeriods =
        telcoPlanningServiceHelper.getAvailablePeriods(tz, purchasedPeriods, months);
    results.put(TelcoPlanningConstants.AVAILABLE_PERIODS, availablePeriods);
    log.debug("PurchasedPeriods: " + purchasedPeriods);
    log.debug("Available Periods: " + availablePeriods);
    return map;
  }

  @Override
  public HashMap<String, String> exportUsersBySites(String userName,
      CellDetailsRequest cellDetailsRequest) throws Exception {
    // Validate the Request Parameters including Filters
    telcoPlanningServiceHelper.validateRequestParameters(cellDetailsRequest, true, false);

    // Authorize request in authsquare if auth is enabled
    if (authEnabled) {
      telcoPlanningServiceHelper.authorizeRequest(userName, cellDetailsRequest);
    }

    // Get the months list where the results are needed
    Set<String> months = DateUtils.getYearMonthList(cellDetailsRequest.getPeriodRange());
    log.info("Months are: " + months);

    // Get user txn collections from months
    String collectionNames = telcoPlanningSolrUtil.getUserTxnCollections(months);

    // Get user txn parent and child filters
    Set<String> parentFilters = telcoPlanningSolrUtil.getUserTxnFilters(cellDetailsRequest);
    Set<String> childFilters = telcoPlanningSolrUtil.getChildFilters(cellDetailsRequest);

    String header = telcoPlanningServiceHelper.buildExportedUsersFileHeader(cellDetailsRequest);
    HashMap<String, String> exportedUsers =
        solrRepository.exportUsersPerSites(collectionNames, parentFilters, childFilters, header);
    return exportedUsers;
  }

  @Override
  public List<RoiNode> getImmediateChildren(String userName, String parentIds, String sourceGeoJson)
      throws ServiceException, BadRequestException {

    log.info("getImmediateChildren for userName: " + userName + "; parentIds: " + parentIds
        + "; sourceGeoJson: " + sourceGeoJson);

    List<RoiNode> roiNodeMOs = new ArrayList<RoiNode>();

    try {
      // validate the input
      if (StringUtils.isEmpty(userName) || "null".equals(userName)) {
        throw new BadRequestException("Invalid userName.");
      }
      if (StringUtils.isEmpty(parentIds) || "null".equals(parentIds)) {
        throw new BadRequestException("Invalid parentIds.");
      }
      if (StringUtils.isEmpty(sourceGeoJson) || "null".equals(sourceGeoJson)) {
        throw new BadRequestException("Invalid sourceGeoJson.");
      }

      // call authsquare to get the response.
      String url = geoHierarchyEndPoint.concat(
          "/getImmediateChildren?parentIds=" + parentIds + "&sourceGeoJson=" + sourceGeoJson);
      ResponseEntity<Object> resp =
          TelcoPlanningServiceHelper.getService(url, Object.class, userName);
      roiNodeMOs = (List<RoiNode>) resp.getBody();
    } catch (BadRequestException e) {
      log.error("BadRequestException in getImmediateChildren: ", e);
      throw e;
    } catch (Exception e) {
      log.error("Error in getting getImmediateChildren.", e);
      throw new ServiceException("Failed to get results from data store." + e.getMessage(), e);
    }
    return roiNodeMOs;
  }

  @Override
  public void generateGeoJson() throws ServiceException {
    geoJsonGenerator.generateGeoJsonUpdated();
  }

}
