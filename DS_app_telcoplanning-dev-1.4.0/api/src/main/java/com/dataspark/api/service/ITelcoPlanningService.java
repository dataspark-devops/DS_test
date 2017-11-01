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
import com.dataspark.api.data.TopRoiStatsResponse;
import com.dataspark.api.exception.BadRequestException;
import com.dataspark.api.exception.ServiceException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.solr.client.solrj.SolrServerException;

/**
 * @author ragarwal
 *
 *         2017-03-01
 */
public interface ITelcoPlanningService {
  TopRoiStatsResponse getTopRoiStats(String reqYearMonth, String userName) throws ServiceException;

  CellDetailsResponse getCellDetailsBySites(String userName, CellDetailsRequest cellDetailsRequest)
      throws Exception;

  Response getSiteDetailsByROI(String userName, CellDetailsRequest cellDetailsRequest)
      throws Exception;

  List<RoiLevelInfo> getGeoHierarchy(String inputRoiId, String inputRoiLevel, String outputRoiLevel)
      throws ServiceException, BadRequestException;

  String getSiteDetails(String month)
      throws SolrServerException, IOException, BadRequestException, ServiceException;

  String autoWarmSitesAPIForTopROI(String month, String roiId)
      throws SolrServerException, IOException, BadRequestException, ServiceException;

  HashMap<String, Object> getModuleSettingsByModuleName(String userName)
      throws SolrServerException, IOException;

  HashMap<String, String> exportUsersBySites(String userName, CellDetailsRequest cellDetailsRequest) throws Exception;

  List<RoiNode> getImmediateChildren(String userName, String parentIds, String sourceGeoJson)
      throws ServiceException, BadRequestException;

  void generateGeoJson() throws ServiceException;

}
