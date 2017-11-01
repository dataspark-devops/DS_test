/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.service;

import com.dataspark.api.data.CellDetailsRequest;
import com.dataspark.api.data.Meta;
import com.dataspark.api.data.Period;
import com.dataspark.api.data.TelcoPlanningConstants;
import com.dataspark.api.exception.BadRequestException;
import com.dataspark.api.exception.ServiceException;
import com.dataspark.api.exceptions.UnAuthorizedException;
import com.dataspark.api.invoker.AuthInvoker;
import com.dataspark.api.invoker.AuthSquareConstants;
import com.dataspark.api.solr.util.DateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import lombok.extern.log4j.Log4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @author ragarwal, 2017-05-17
 */
@Log4j
@Service
public class TelcoPlanningServiceHelper {

  @Autowired
  private FilterService filterService;

  @Autowired
  private AuthInvoker authInvoker;

  @Autowired
  private SolrToJsonPropMappingService solrToJsonPropSvc;

  public Meta buildSiteDetailsResponseMeta(CellDetailsRequest cdr) {
    Meta meta = new Meta();
    meta.setRoiFilters(cdr.getRoiFilters());
    meta.setPeriodType(cdr.getPeriodType());
    meta.setPeriodRange(cdr.getPeriodRange());
    meta.setIndicators(cdr.getIndicators());
    meta.setFilterUser(cdr.getFilterUser());
    meta.setFilterDevice(cdr.getFilterDevice());
    meta.setFilterCell(cdr.getFilterCell());
    meta.setSites(cdr.getSites());
    meta.setOrder(getHeaderArray(solrToJsonPropSvc.siteInfoJsonPropIndexMap));
    return meta;
  }

  public List<Object[]> buildSiteDetailsResponse(Map<String, Map<String, Object>> siteHomeWorkMap,
      List<Object[]> siteResponseList, Map<String, Map<String, Object>> siteDetailsMap,
      List<String> indicators, String periodType) {

    List<String> staticSiteInfoJsonProps =
        new ArrayList<>(solrToJsonPropSvc.cellCollSiteInfoMap.values());
    staticSiteInfoJsonProps.remove(TelcoPlanningConstants.OutputMapField.ID);

    if (!CollectionUtils.isEmpty(siteResponseList)) {
      for (Object[] calculatedSiteInfo : siteResponseList) {
        String siteId = String.valueOf(calculatedSiteInfo[0]);

        // populate static site info
        if (!siteDetailsMap.isEmpty()) {
          Map<String, Object> staticSiteInfo = siteDetailsMap.get(siteId);
          if (staticSiteInfo != null) {
            for (String jsonProp : staticSiteInfoJsonProps) {
              calculatedSiteInfo[solrToJsonPropSvc.siteInfoJsonPropIndexMap.get(jsonProp)] =
                  staticSiteInfo.get(jsonProp);
            }

            // aggregate requested indicators like revenue, payload from static cell info if
            // requested
            // period type is month
            if (TelcoPlanningConstants.PeriodType.MONTH.equalsIgnoreCase(periodType)) {
              Map<String, Object> allStaticCellInfoMap = (Map<String, Object>) staticSiteInfo
                  .get(TelcoPlanningConstants.OutputMapField.CELL_STATS);
              if (allStaticCellInfoMap != null) {
                for (String jsonProp : solrToJsonPropSvc.cellCollIndicatorsFormulaMap.keySet()) {
                  if (indicators.contains(jsonProp)) {
                    // aggregate the value of this requested json property.
                    calculatedSiteInfo[solrToJsonPropSvc.siteInfoJsonPropIndexMap.get(jsonProp)] =
                        aggregateIndicatorValue(jsonProp, allStaticCellInfoMap.values());
                  }
                }
              }
            }
          }
        }

        // populate home work info
        if (siteHomeWorkMap != null && !siteHomeWorkMap.isEmpty()) {
          Map<String, Object> hwCount =
              populateHomeWorkIndicators(siteHomeWorkMap.get(siteId), indicators);

          for (Entry<String, Object> hwEntry : hwCount.entrySet()) {
            calculatedSiteInfo[solrToJsonPropSvc.siteInfoJsonPropIndexMap.get(hwEntry.getKey())] =
                hwEntry.getValue();
          }
        }
      }
    }

    log.info("Sites API: Built Cell details response with size: " + siteResponseList.size());
    return siteResponseList;
  }

  private Object aggregateIndicatorValue(String jsonProp, Collection<Object> values) {
    // each value is a Map<String, Object> and contains payload, revenue and other cell indicators.
    Double aggValue = 0d;
    List<Double> allValues = new ArrayList<Double>();
    String requestedAggType = solrToJsonPropSvc.cellCollIndicatorsAggTypeMap.get(jsonProp);

    if (TelcoPlanningConstants.AggregationType.SUM.equals(requestedAggType)
        || TelcoPlanningConstants.AggregationType.AVERAGE.equals(requestedAggType)
        || TelcoPlanningConstants.AggregationType.MEDIAN.equals(requestedAggType)) {
      for (Object value : values) {
        Map<String, Object> staticCellInfo = (Map<String, Object>) value;
        Double indicatorValue = Double.valueOf(staticCellInfo.get(jsonProp) == null ? "0"
            : String.valueOf(staticCellInfo.get(jsonProp)));
        aggValue += indicatorValue;
        allValues.add(indicatorValue);
      }
    }

    if (TelcoPlanningConstants.AggregationType.AVERAGE.equals(requestedAggType)) {
      aggValue = aggValue / values.size();
    }

    if (TelcoPlanningConstants.AggregationType.MEDIAN.equals(requestedAggType)) {
      Collections.sort(allValues);
      if ((values.size() % 2) == 0) {
        // even values
        int mid = values.size() / 2;
        aggValue = (allValues.get(mid) + allValues.get(mid - 1)) / 2;
      } else {
        // odd values
        aggValue = allValues.get(values.size() / 2);
      }
    }

    return aggValue;
  }

  public List<Map<String, Object>> buildCellDetailsReponse(
      Map<String, Map<String, Object>> siteHomeWorkMap, List<Map<String, Object>> cellStats,
      Map<String, Map<String, Object>> siteDetailsMap, List<String> indicators, String periodType) {

    List<String> cellIndicatorsToSkip = new ArrayList<String>();
    for (String indicator : solrToJsonPropSvc.cellCollIndicatorsFormulaMap.keySet()) {
      if (!indicators.contains(indicator)) {
        cellIndicatorsToSkip.add(indicator);
      }
    }

    if (!CollectionUtils.isEmpty(cellStats)) {
      for (Map<String, Object> calculatedSiteInfo : cellStats) {
        // lat_lon_s
        String siteId =
            String.valueOf(calculatedSiteInfo.get(TelcoPlanningConstants.OutputMapField.SITE_ID));

        // contains calculated fields like sms, calls, unique_people, stay duration
        List<Map<String, Object>> allCalculatedCellInfo =
            (List<Map<String, Object>>) calculatedSiteInfo
                .get(TelcoPlanningConstants.OutputMapField.CELL_STATS);

        // populate static site info
        if (!siteDetailsMap.isEmpty()) {
          Map<String, Object> staticSiteInfo = siteDetailsMap.get(siteId);
          if (staticSiteInfo != null) {
            for (String jsonProp : solrToJsonPropSvc.cellCollSiteInfoMap.values()) {
              calculatedSiteInfo.put(jsonProp, staticSiteInfo.get(jsonProp));
            }

            // reset site id as the id (lat_lon)
            String[] latLonArr =
                String.valueOf(calculatedSiteInfo.get(TelcoPlanningConstants.OutputMapField.ID))
                    .split(TelcoPlanningConstants.Delimiters.UNDERSCORE);
            calculatedSiteInfo.put(TelcoPlanningConstants.OutputMapField.LATITUDE,
                Double.valueOf(latLonArr[0]));
            calculatedSiteInfo.put(TelcoPlanningConstants.OutputMapField.LONGITUDE,
                Double.valueOf(latLonArr[1]));

            // populate static cell info like revenue, payload
            Map<String, Object> allStaticCellInfo = (Map<String, Object>) staticSiteInfo
                .get(TelcoPlanningConstants.OutputMapField.CELL_STATS);
            if (!allStaticCellInfo.isEmpty() && !CollectionUtils.isEmpty(allCalculatedCellInfo)) {
              for (Map<String, Object> calculatedCellInfo : allCalculatedCellInfo) {
                Map<String, Object> staticCellInfo = (Map<String, Object>) allStaticCellInfo
                    .get(calculatedCellInfo.get(TelcoPlanningConstants.OutputMapField.CELL_ID));
                if (staticCellInfo != null) {
                  for (String jsonProp : solrToJsonPropSvc.cellCollCellInfoMap.values()) {
                    if (!solrToJsonPropSvc.cellCollIndicatorsFormulaMap.keySet()
                        .contains(jsonProp)) {
                      // non-indicator
                      calculatedCellInfo.put(jsonProp, staticCellInfo.get(jsonProp));
                    } else if (!cellIndicatorsToSkip.contains(jsonProp)
                        && TelcoPlanningConstants.PeriodType.MONTH.equalsIgnoreCase(periodType)) {
                      calculatedCellInfo.put(jsonProp,
                          staticCellInfo.get(jsonProp) == null ? 0 : staticCellInfo.get(jsonProp));
                    }
                  }
                }
              }
            }
          }
        }

        // populate home work info
        if (siteHomeWorkMap != null && !siteHomeWorkMap.isEmpty()) {
          Map<String, Object> hwCount =
              populateHomeWorkIndicators(siteHomeWorkMap.get(siteId), indicators);
          calculatedSiteInfo.putAll(hwCount);
        }
      }
    }

    log.info("Cells API: Built Cell details response with size: " + cellStats.size());
    return cellStats;

  }

  private Map<String, Object> populateHomeWorkIndicators(Map<String, Object> hwCountForSite,
      List<String> indicators) {
    Map<String, Object> hwResponse = new HashMap<String, Object>();

    // home and work count information exists for the site id
    if (hwCountForSite != null) {
      for (String indicatorName : TelcoPlanningConstants.Indicators.getUserTxnParentIndicators()) {
        if (indicators.contains(indicatorName)) {
          Object indicatorValue = hwCountForSite.get(indicatorName);
          hwResponse.put(indicatorName, indicatorValue == null ? 0 : indicatorValue);
        }
      }
    } else {
      for (String indicatorName : TelcoPlanningConstants.Indicators.getUserTxnParentIndicators()) {
        if (indicators.contains(indicatorName)) {
          hwResponse.put(indicatorName, 0);
        }
      }
    }

    return hwResponse;
  }

  public void validateRequestParameters(CellDetailsRequest cellDetailsRequest, boolean checkSites,
      boolean checkIndicators) throws BadRequestException {
    try {

      // Validate the request Params
      Preconditions.checkNotNull(cellDetailsRequest.getRoiFilters(), "roiFilters are missing");
      Preconditions.checkArgument(cellDetailsRequest.getRoiFilters().size() > 0,
          "roiFilters are missing");
      validatePeriod(cellDetailsRequest.getPeriodType(), cellDetailsRequest.getPeriodRange());
      if (checkIndicators) {
        Preconditions.checkNotNull(cellDetailsRequest.getIndicators(),
            "indicators field is missing");
        Preconditions.checkArgument(cellDetailsRequest.getIndicators().size() > 0,
            "indicators field is blank");
      }
      if (checkSites) {
        Preconditions.checkArgument(!CollectionUtils.isEmpty(cellDetailsRequest.getSites()),
            "sites field is invalid");
      }
      // Validate the filters
      if (cellDetailsRequest.getFilterCell() != null
          && !filterService.validateFilters(cellDetailsRequest.getFilterCell())) {
        throw new BadRequestException("Invalid entries inside Cell Filters");
      }
      if (cellDetailsRequest.getFilterDevice() != null
          && !filterService.validateFilters(cellDetailsRequest.getFilterDevice())) {
        throw new BadRequestException("Invalid entries inside Device Filters");
      }
      if (cellDetailsRequest.getFilterUser() != null) {
        List<String> homeLocationFilter =
            cellDetailsRequest.getFilterUser().get(TelcoPlanningConstants.HOME_LOCATION_FILTER);
        List<String> workLocationFilter =
            cellDetailsRequest.getFilterUser().get(TelcoPlanningConstants.WORK_LOCATION_FILTER);

        if (!CollectionUtils.isEmpty(homeLocationFilter)) {
          // process home location filter and convert to solr filter
          cellDetailsRequest.getFilterUser().putAll(
              processLocationFilter(homeLocationFilter, TelcoPlanningConstants.LocationType.HOME));
          cellDetailsRequest.getFilterUser().remove(TelcoPlanningConstants.HOME_LOCATION_FILTER);
        }

        if (!CollectionUtils.isEmpty(workLocationFilter)) {
          // process work location filter and convert to solr filter
          cellDetailsRequest.getFilterUser().putAll(
              processLocationFilter(workLocationFilter, TelcoPlanningConstants.LocationType.WORK));
          cellDetailsRequest.getFilterUser().remove(TelcoPlanningConstants.WORK_LOCATION_FILTER);
        }

        if (!filterService.validateFilters(cellDetailsRequest.getFilterUser())) {
          throw new BadRequestException("Invalid entries inside User Filters");
        }
      }
    } catch (Exception e) {
      log.error("Error in getCellDetails: ", e);
      throw new BadRequestException(e.getMessage());
    }
  }

  private Map<String, List<String>> processLocationFilter(List<String> roiList,
      String locationType) {
    Map<String, List<String>> filters = new HashMap<String, List<String>>();
    for (String roi : roiList) {
      String[] splits = roi.split(TelcoPlanningConstants.Delimiters.COLON);
      String roiLevel =
          locationType.concat(TelcoPlanningConstants.Delimiters.UNDERSCORE).concat(splits[0]);
      List<String> values = new ArrayList<String>();
      if (filters.containsKey(roiLevel)) {
        values = filters.get(roiLevel);
      }
      values.add(splits[1]);
      filters.put(roiLevel, values);
    }
    return filters;
  }

  private void validatePeriod(String type, List<Period> range)
      throws BadRequestException, ParseException {
    Preconditions.checkArgument(!StringUtils.isEmpty(type), "periodType field is invalid");
    Preconditions.checkArgument(!CollectionUtils.isEmpty(range), "periodRange field is invalid");
    Preconditions.checkArgument(
        TelcoPlanningConstants.PeriodType.getAvailablePeriodTypes().contains(type),
        "Invalid period type. Allowed values are week and month.");

    for (Period period : range) {
      String startDateStr = period.getStartDate().substring(0, 10);
      String endDateStr = period.getEndDate().substring(0, 10);
      DateTime startDate = new DateTime(startDateStr);
      DateTime endDate = new DateTime(endDateStr);

      Integer allowedNumDays = TelcoPlanningConstants.PeriodType.WEEK.equals(type)
          ? TelcoPlanningConstants.NUMBER_OF_DAYS_IN_WEEK
          : DateUtils.getNumberOfDaysInMonthForDate(startDate.toDate());
      if (allowedNumDays != Days.daysBetween(new LocalDate(startDate), new LocalDate(endDate))
          .getDays()) {
        throw new BadRequestException("Invalid period range for requested period type.");
      }
    }
  }

  public String buildQuery(String inputRoiId, String inputRoiLevel, String outputRoiLevel,
      LinkedHashMap<String, String> roiLevelsMap) {
    String levels[] = roiLevelsMap.keySet().toArray(new String[4]);
    Integer inputRoiLevelNum = getOrderForRoiLevel(inputRoiLevel, levels);
    Integer outputRoiLevelNum = getOrderForRoiLevel(outputRoiLevel, levels);

    StringBuilder sb = new StringBuilder();

    for (int i = 0; i <= outputRoiLevelNum; i++) {
      sb.append(roiLevelsMap.get(levels[i]));
      if (i == inputRoiLevelNum && !TelcoPlanningConstants.UNKNOWN_ID.equals(inputRoiId)) {
        sb.append("[?(@.id == " + inputRoiId + ")].");
      } else {
        sb.append("[*].");
      }
    }
    String query = sb.toString().substring(0, sb.toString().length() - 1) + "['name','id']";
    log.debug("GeoJson Query: " + query);
    return query;
  }

  private int getOrderForRoiLevel(String roiLevel, String[] levels) {
    for (int i = 0; i < levels.length; i++) {
      if (roiLevel.equalsIgnoreCase(levels[i])) {
        return i;
      }
    }
    return -1;
  }

  public String buildExportedUsersFileHeader(CellDetailsRequest cdr) {
    StringBuffer sb = new StringBuffer();
    sb.append("Exported %userscount% users for: \nROI: "
        + cdr.getRoiFilters().values().toString().replace("[", "").replace("]", "") + "\nSites: "
        + cdr.getSites().toString().replace("[", "").replace("]", ""));
    sb.append("\nPeriod type: " + cdr.getPeriodType() + "\nPeriod Range:");
    for (Period pmo : cdr.getPeriodRange()) {
      sb.append("\n\t" + pmo.getStartDate() + " to " + pmo.getEndDate());
    }
    sb.append(getFiltersAsString(cdr.getFilterUser(), "\nUser Filter(s):"));
    sb.append(getFiltersAsString(cdr.getFilterCell(), "\nCell Filter(s):"));
    sb.append(getFiltersAsString(cdr.getFilterDevice(), "\nDevice Filter(s):"));
    sb.append(
        "\n\n" + StringUtils.join(getHeaderArray(solrToJsonPropSvc.userProfileJsonPropIndexMap),
            TelcoPlanningConstants.Delimiters.PIPE));
    return sb.toString();
  }

  private String getFiltersAsString(Map<String, List<String>> filters, String filterType) {
    StringBuffer sb = new StringBuffer();
    if (filters != null) {
      sb.append(filterType);
      for (Entry<String, List<String>> entry : filters.entrySet()) {
        sb.append("\n\t" + entry.getKey() + ": " + entry.getValue());
      }
    }
    return sb.toString();
  }

  public List<Map<String, String>> getAvailablePeriods(DateTimeZone tz,
      List<Map<String, Object>> purchasedPeriods, List<Integer> months) {
    List<Map<String, String>> availablePeriods = new ArrayList<Map<String, String>>();

    if (purchasedPeriods != null && purchasedPeriods.size() != 0) {
      boolean isUnlimited =
          ((boolean) purchasedPeriods.get(0).get(TelcoPlanningConstants.IS_UNLIMITED));
      if (isUnlimited) {
        // If the user is unlimited user Just return the months range collections from solr
        for (Integer availableMonth : months) {
          Map<String, String> datesMap = new HashMap<String, String>();
          String availableMonthStr = availableMonth.toString();
          int collectionStartYear = Integer.parseInt(availableMonthStr.substring(0, 4));
          int collectionStartMonth = Integer.parseInt(availableMonthStr.substring(4));
          DateTime availableStartDate =
              new DateTime(collectionStartYear, collectionStartMonth, 1, 0, 0, tz);
          DateTime availableEndDate = availableStartDate.plusMonths(1);
          datesMap.put(TelcoPlanningConstants.START_DATE, availableStartDate.toString());
          datesMap.put(TelcoPlanningConstants.END_DATE, availableEndDate.toString());
          availablePeriods.add(datesMap);
        }
      } else {
        // Check the intersection
        Map<String, Object> purchasedPeriod = purchasedPeriods.get(0);
        DateTime purchasedStartDate =
            new DateTime(purchasedPeriod.get(TelcoPlanningConstants.START_DATE));
        DateTime purchasedEndDate =
            new DateTime(purchasedPeriod.get(TelcoPlanningConstants.END_DATE));

        for (Integer availableMonth : months) {
          Map<String, String> datesMap = new HashMap<String, String>();
          String availableMonthStr = availableMonth.toString();
          int collectionStartYear = Integer.parseInt(availableMonthStr.substring(0, 4));
          int collectionStartMonth = Integer.parseInt(availableMonthStr.substring(4));
          DateTime availableStartDate =
              new DateTime(collectionStartYear, collectionStartMonth, 1, 0, 0, tz);
          DateTime availableEndDate = availableStartDate.plusMonths(1);

          // Case 1: No intersection -
          // (available start date > purchased end date) OR
          // (available end date < purchased start date)
          if ((availableStartDate.compareTo(purchasedEndDate.minusDays(1)) > 0)
              || (availableEndDate.minusDays(1).compareTo(purchasedStartDate) < 0)) {
            // No need to add to availablePeriods
            continue;
          } else {
            datesMap.put(TelcoPlanningConstants.START_DATE,
                (availableStartDate.compareTo(purchasedStartDate) >= 0)
                    ? availableStartDate.toString() : purchasedStartDate.toString());

            datesMap.put(TelcoPlanningConstants.END_DATE,
                (availableEndDate.compareTo(purchasedEndDate) <= 0) ? availableEndDate.toString()
                    : purchasedEndDate.toString());
            availablePeriods.add(datesMap);
          }
        }
      }
    }
    return availablePeriods;
  }

  public static <T> ResponseEntity<T> getService(String url, Class<T> type, String userName) {

    RestTemplate restTemplate = new RestTemplate();
    ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setConnectTimeout(20000000);
    ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(20000000);

    ResponseEntity<T> result;
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-user", userName);
    HttpEntity<HashMap<String, Object>> entity = new HttpEntity<HashMap<String, Object>>(headers);
    result = restTemplate.exchange(url, HttpMethod.GET, entity, type);
    return result;
  }

  public boolean authorizeRequest(String userName, CellDetailsRequest cdr) throws ServiceException {
    // add dates to authorize
    for (Period period : cdr.getPeriodRange()) {
      authInvoker.addDatesToAuthorize(period.getStartDate(), period.getEndDate());
    }

    // add elements to authorize
    authInvoker.setROILayersToAuthorize(cdr.getRoiFilters().keySet());

    Set<String> allRoiValues = new HashSet<String>();
    for (List<String> roiValues : cdr.getRoiFilters().values()) {
      allRoiValues.addAll(new HashSet<String>(roiValues));
    }
    allRoiValues.remove("*");
    authInvoker.setROIValuesToAuthorize(allRoiValues);
    authInvoker.setIndicatorsToAuthorize(new HashSet<String>(cdr.getIndicators()));

    // FILTERS
    Set<String> allFilters = new HashSet<String>();
    if (cdr.getFilterUser() != null && !cdr.getFilterUser().isEmpty()) {
      Set<String> userFilterKeys = processUserHomeWorkLocationFilters(cdr.getFilterUser().keySet());
      allFilters.addAll(userFilterKeys);
    }

    if (cdr.getFilterCell() != null && !cdr.getFilterCell().isEmpty()) {
      allFilters.addAll(cdr.getFilterCell().keySet());
    }
    if (cdr.getFilterDevice() != null && !cdr.getFilterDevice().isEmpty()) {
      allFilters.addAll(cdr.getFilterDevice().keySet());
    }

    if (!CollectionUtils.isEmpty(allFilters)) {
      authInvoker.setFiltersToAuthorize(allFilters);
    }

    try {
      return authInvoker.authorize(userName);
    } catch (UnAuthorizedException e) {
      log.error("UnAuthorizedException in authorizeRequest: ", e);
      throw e;
    } catch (Exception e) {
      log.error("Exception in authorizeRequest: ", e);
      throw new ServiceException("Exception in authorizeRequest: ", e);
    }
  }

  private Set<String> processUserHomeWorkLocationFilters(Set<String> userFilterKeys) {
    List<String> keys = new ArrayList<>(userFilterKeys);
    boolean home = false;
    boolean work = false;
    home = home || keys.removeIf(key -> key.startsWith("home_roi"));
    work = work || keys.removeIf(key -> key.startsWith("work_roi"));
    if (home)
      keys.add(TelcoPlanningConstants.HOME_LOCATION_FILTER);
    if (work)
      keys.add(TelcoPlanningConstants.WORK_LOCATION_FILTER);
    return new HashSet<>(keys);
  }

  public HashMap<String, Set<String>> filterKeys(String userName,
      HashMap<String, Set<String>> allKeysMap) throws ServiceException {
    try {
      for (Entry<String, Set<String>> entry : allKeysMap.entrySet()) {
        Set<String> values = entry.getValue();
        values.remove("*");
        authInvoker.addElementsToAuthorize(entry.getKey(), values);
      }
      return authInvoker.filterKeys(userName);
    } catch (Exception e) {
      log.error("ServiceException in authorizeRequest: ", e);
      throw new ServiceException(e.getMessage(), e);
    }
  }

  public List<Map<String, Object>> getFilteredTopRoiStats(String userName,
      List<Map<String, Object>> topRoiStats) throws ServiceException {
    Set<String> keys = getTopRoiIds(topRoiStats);
    HashMap<String, Set<String>> filterMap = new HashMap<String, Set<String>>();
    filterMap.put(AuthSquareConstants.Settings.ROI_VALUE, keys);
    HashMap<String, Set<String>> filteredMap = filterKeys(userName, filterMap);
    return getTopRoiStats(
        new ArrayList<String>(filteredMap.get(AuthSquareConstants.Settings.ROI_VALUE)),
        topRoiStats);
  }

  private List<Map<String, Object>> getTopRoiStats(List<String> keys,
      List<Map<String, Object>> topRoiStats) {
    List<Map<String, Object>> filteredRoiStats = new ArrayList<Map<String, Object>>();

    for (int i = 0; i < topRoiStats.size(); i++) {
      Map<String, Object> roiNode = new ObjectMapper().convertValue(topRoiStats.get(i), Map.class);
      if (keys.contains(roiNode.get(TelcoPlanningConstants.OutputMapField.ROI_ID))) {
        filteredRoiStats.add(roiNode);
      }
    }

    return filteredRoiStats;
  }

  private Set<String> getTopRoiIds(List<Map<String, Object>> topRoiStats) {
    Set<String> keys = new HashSet<String>();

    for (int i = 0; i < topRoiStats.size(); i++) {
      Map<String, Object> roiNode = new ObjectMapper().convertValue(topRoiStats.get(i), Map.class);
      keys.add(String.valueOf(roiNode.get(TelcoPlanningConstants.OutputMapField.ROI_ID)));
    }

    return keys;
  }

  private String[] getHeaderArray(Map<String, Integer> jsonPropIndexMap) {
    String[] header = new String[jsonPropIndexMap.size()];
    for (Entry<String, Integer> entry : jsonPropIndexMap.entrySet()) {
      header[entry.getValue()] = entry.getKey();
    }
    return header;
  }

}
