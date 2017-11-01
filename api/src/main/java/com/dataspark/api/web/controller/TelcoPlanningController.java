/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.web.controller;

import com.dataspark.api.data.CellDetailsRequest;
import com.dataspark.api.data.CellDetailsResponse;
import com.dataspark.api.data.Response;
import com.dataspark.api.data.RoiLevelInfo;
import com.dataspark.api.data.RoiNode;
import com.dataspark.api.data.TopRoiStatsResponse;
import com.dataspark.api.exception.BadRequestException;
import com.dataspark.api.exceptions.UnAuthorizedException;
import com.dataspark.api.service.ITelcoPlanningService;
import com.dataspark.api.solr.util.JsonUtil;
import java.util.HashMap;
import java.util.List;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

/**
 * @author ragarwal, 2017-03-01
 */
@Controller
@Component
@EnableAutoConfiguration
@RequestMapping(value = "/v1/telco-network-planning")
@Log4j
@Scope("prototype")
public class TelcoPlanningController {

  @Autowired
  private ITelcoPlanningService telcoPlanningService;

  @RequestMapping(value = "/getTopRoiStats", method = {RequestMethod.GET})
  public ResponseEntity<String> getTopROIStats(
      @RequestHeader(required = false, value = "X-user") final String userName,
      @RequestParam(value = "yearMonth", required = false) String reqYearMonth) {
    try {
      TopRoiStatsResponse topRoiStatsResponse =
          telcoPlanningService.getTopRoiStats(reqYearMonth, userName);
      return new ResponseEntity<String>(
          JsonUtil.OBJECT_MAPPER.writeValueAsString(topRoiStatsResponse), HttpStatus.OK);
    } catch (Exception e) {
      log.error("Error in getTopROIStats: ", e);
      return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

  }

  @RequestMapping(value = "/getCellDetailsBySites", method = {RequestMethod.POST},
      consumes = "application/json")
  public ResponseEntity<String> getCellDetailsBySites(
      @RequestHeader(required = false, value = "X-user") final String userName,
      @Validated @RequestBody(required = true) final CellDetailsRequest cellDetailsRequest)
      throws Exception {

    try {
      log.info("Received request for getCellDetailsBySites: " + cellDetailsRequest.toString());
      CellDetailsResponse cellDetails =
          telcoPlanningService.getCellDetailsBySites(userName, cellDetailsRequest);
      return new ResponseEntity<String>(JsonUtil.OBJECT_MAPPER.writeValueAsString(cellDetails),
          HttpStatus.OK);
    } catch (BadRequestException e) {
      log.error("Bad Request Exception: ", e);
      return new ResponseEntity<String>("Bad Request: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (UnAuthorizedException e) {
      log.error("UnAuthorized Exception: ", e);
      return new ResponseEntity<String>(e.getMessage(), HttpStatus.FORBIDDEN);
    } catch (Exception e) {
      log.error("Error in getCellDetailsByROI: ", e);
      return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

  }

  @RequestMapping(value = "/getSiteDetailsByROI", method = {RequestMethod.POST},
      consumes = "application/json")
  public ResponseEntity<String> getSiteDetailsByROI(
      @RequestHeader(required = false, value = "X-user") final String userName,
      @Validated @RequestBody(required = true) final CellDetailsRequest cellDetailsRequest)
      throws Exception {

    try {
      log.info("Received request for getSiteDetailsByROI: " + cellDetailsRequest);
      Response siteDetails = telcoPlanningService.getSiteDetailsByROI(userName, cellDetailsRequest);
      return new ResponseEntity<String>(JsonUtil.OBJECT_MAPPER.writeValueAsString(siteDetails),
          HttpStatus.OK);
    } catch (BadRequestException e) {
      log.error("Bad Request Exception: ", e);
      return new ResponseEntity<String>("Bad Request: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (UnAuthorizedException e) {
      log.error("UnAuthorized Exception: ", e);
      return new ResponseEntity<String>(e.getMessage(), HttpStatus.FORBIDDEN);
    } catch (Exception e) {
      log.error("Error in getCellDetailsByROI: ", e);
      return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

  }

  @RequestMapping(value = "/getGeoHierarchy", method = {RequestMethod.GET})
  public ResponseEntity<String> getGeoHierarchy(
      @RequestParam(value = "inputRoiId") String inputRoiId,
      @RequestParam(value = "inputRoiLevel") String inputRoiLevel,
      @RequestParam(value = "outputRoiLevel") String outputRoiLevel) {
    try {
      List<RoiLevelInfo> roiLevelInfoMOs =
          telcoPlanningService.getGeoHierarchy(inputRoiId, inputRoiLevel, outputRoiLevel);
      return new ResponseEntity<String>(JsonUtil.OBJECT_MAPPER.writeValueAsString(roiLevelInfoMOs),
          HttpStatus.OK);
    } catch (BadRequestException e) {
      log.error("Error in getGeoHierarchy: ", e);
      return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      log.error("Error in getGeoHierarchy: ", e);
      return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }


  @RequestMapping(value = "/autoWarmCellSiteDetails", method = {RequestMethod.GET})
  public ResponseEntity<String> autoWarmCellSiteDetails(
      @RequestParam(value = "month") String month) {
    try {
      String status = telcoPlanningService.getSiteDetails(month);
      return new ResponseEntity<String>(status, HttpStatus.OK);
    } catch (BadRequestException e) {
      log.error("Error in autoWarmCellDetails: ", e);
      return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      log.error("Error in autoWarmCellDetails: ", e);
      return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }


  @RequestMapping(value = "/autoWarmSitesAPIForTopROI", method = {RequestMethod.GET})
  public ResponseEntity<String> autoWarmSitesAPIForTopROI(
      @RequestParam(value = "month") String month, @RequestParam(value = "roiId") String roiId) {
    try {
      String status = telcoPlanningService.autoWarmSitesAPIForTopROI(month, roiId);
      return new ResponseEntity<String>(status, HttpStatus.OK);
    } catch (BadRequestException e) {
      log.error("Error in autoWarmSitesAPIForTopROI: ", e);
      return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      log.error("Error in autoWarmSitesAPIForTopROI: ", e);
      return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }


  @RequestMapping(value = "/settings", method = {RequestMethod.GET})
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<String> getModuleSettingsByModuleName(
      @RequestHeader(required = true, value = "X-user") final String userName) {
    try {
      HashMap<String, Object> resp = telcoPlanningService.getModuleSettingsByModuleName(userName);
      return new ResponseEntity<String>(JsonUtil.OBJECT_MAPPER.writeValueAsString(resp),
          HttpStatus.OK);
    } catch (HttpClientErrorException e) {
      log.error("Exception in get module settings: ", e);
      return new ResponseEntity<String>(e.getMessage(), HttpStatus.FORBIDDEN);
    } catch (Exception e) {
      log.error("Exception in get module settings: ", e);
      return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }


  @RequestMapping(value = "/exportUsersBySites", method = {RequestMethod.POST},
      consumes = "application/json")
  public ResponseEntity<String> exportUsersBySitesTest(
      @RequestHeader(required = false, value = "X-user") final String userName,
      @Validated @RequestBody(required = true) final CellDetailsRequest cellDetailsRequest)
      throws Exception {
    try {
      log.info("Received request for exportUsersBySites: " + cellDetailsRequest.toString());
      HashMap<String, String> resp =
          telcoPlanningService.exportUsersBySites(userName, cellDetailsRequest);
      return new ResponseEntity<String>(JsonUtil.OBJECT_MAPPER.writeValueAsString(resp),
          HttpStatus.OK);
    } catch (BadRequestException e) {
      log.error("Bad Request Exception: ", e);
      return new ResponseEntity<String>("Bad Request: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      log.error("Error in exportUsersBySites: ", e);
      return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @RequestMapping(value = "/getImmediateChildren", method = {RequestMethod.GET})
  public ResponseEntity<String> getImmediateChildren(
      @RequestHeader(required = true, value = "X-user") final String userName,
      @RequestParam(value = "parentIds") String parentIds,
      @RequestParam(value = "sourceGeoJson") String sourceGeoJson) {
    try {
      List<RoiNode> roiNodeMOs =
          telcoPlanningService.getImmediateChildren(userName, parentIds, sourceGeoJson);
      return new ResponseEntity<String>(JsonUtil.OBJECT_MAPPER.writeValueAsString(roiNodeMOs),
          HttpStatus.OK);
    } catch (BadRequestException e) {
      log.error("Error in getImmediateChildren: ", e);
      return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      log.error("Error in getImmediateChildren: ", e);
      return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @RequestMapping(value = "/generateGeoJson", method = {RequestMethod.GET})
  public ResponseEntity<String> generateGeoJson() {
    try {
      telcoPlanningService.generateGeoJson();
      return new ResponseEntity<String>("Successfully generated the geo json.", HttpStatus.OK);
    } catch (Exception e) {
      log.error("Error in generateGeoJson: ", e);
      return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
