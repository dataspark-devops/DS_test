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
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.solr.client.solrj.SolrServerException;

/**
 * @author ragarwal
 *
 *         2017-03-01
 */
public interface ISolrRepository {
  TopRoiStatsResponse getTopRoiStats(String reqYearMonth) throws SolrServerException, IOException;

  Map<String, Map<String, Object>> getHomeWorkDetails(List<String> indicators,
      String collectionNames, Set<String> parentFilters, Set<String> childFilters)
      throws SolrServerException, IOException, BadRequestException, ServiceException;

  Map<String, Map<String, Object>> getSiteDetails(String month)
      throws SolrServerException, IOException, BadRequestException, ServiceException;

  List<Map<String, Object>> getCellStats(List<String> indicators, String collectionNames,
      Set<String> parentFilters, Set<String> childFilters)
      throws SolrServerException, IOException, BadRequestException, ServiceException;

  List<Object[]> getSiteStats(List<String> indicators, String collectionNames,
      Set<String> parentFilters, Set<String> childFilters)
      throws SolrServerException, IOException, BadRequestException, ServiceException;

  Map<String, Map<String, Object>> getHomeWorkDetailsWithoutFilters(List<String> indicators,
      String collectionNames, Set<String> parentFilters, Set<String> childFilters)
      throws SolrServerException, IOException, BadRequestException, ServiceException;

  List<Object[]> getSiteStatsWithoutFilters(List<String> indicators, String collectionNames,
      Set<String> parentFilters, Set<String> childFilters)
      throws SolrServerException, IOException, BadRequestException, ServiceException;

  List<Integer> getAvailableCellCollectionMonthsInSolr() throws SolrServerException, IOException;

  HashMap<String, String> exportUsersPerSites(String collectionNames, Set<String> parentFilters,
      Set<String> childFilters, String header)
      throws SolrServerException, IOException, BadRequestException, ServiceException;
}
