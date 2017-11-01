/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.web.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dataspark.api.data.RoiLevelInfo;
import com.dataspark.api.solr.util.GeoHierarchyGenerator;
import com.dataspark.api.solr.util.JsonUtil;
import com.dataspark.spring.App;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;

/**
 * @author ragarwal, 2017-03-01
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(classes = {TelcoPlanningControllerTest.class, App.class})
@SuppressWarnings("unchecked")
public class TelcoPlanningControllerTest extends BaseSolrControllerTest {

  private String testBaseUrl = baseUrl + "/telco-network-planning/";

  @Value("${collection.toproistats.prefix:top_roi_stats_}")
  private String topRoiStatsCollectionPrefix;

  @Value("${telcoplanning.geojson.updated}")
  private String geoJsonGeneratedFile;

  @Before
  public void before() throws IOException, SolrServerException {
    deleteAllCollections();
  }

  private String latestMonth = "201705";
  private String previousMonth = "201704";
  
  @Test
  public void testGetRoiStatsFromPreviousMonthData() throws Exception {
    String collectionName = topRoiStatsCollectionPrefix + previousMonth;
    setupCollections(collectionName);

    String expectedJson = readJson("roiStatsFrom201704.json");
    ResultActions response = getResponseResultFor(testBaseUrl.concat("getTopRoiStats")).andExpect(status().isOk());
    response.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetRoiStatsFromLatestMonthData() throws Exception {
    // previous month collection does not exist, so fetch results from latest collection
    String collectionName = topRoiStatsCollectionPrefix + latestMonth;
    setupCollections(collectionName);

    String expectedJson = readJson("roiStatsFrom201705.json");
    ResultActions response = getResponseResultFor(testBaseUrl.concat("getTopRoiStats")).andExpect(status().isOk());
    response.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetRoiStatsCollectionNotFound() throws Exception {
    List<Map<String, Object>> roiStatsMO = new ArrayList<Map<String, Object>>();
    String response = getResponseFor(testBaseUrl.concat("getTopRoiStats"));
    JSONObject json = (JSONObject) new JSONParser().parse(response);
    roiStatsMO = JsonUtil.OBJECT_MAPPER.convertValue(json.get("results"), roiStatsMO.getClass());
    assertEquals(0, roiStatsMO.size());
  }

  @Test
  public void testGetRoiStatsForSpecifiedYearAndMonth() throws Exception {
    String collectionName = topRoiStatsCollectionPrefix + previousMonth;
    setupCollections(collectionName);

    String expectedJson = readJson("roiStatsFrom201704.json");
    ResultActions response = getResponseResultFor(testBaseUrl.concat("getTopRoiStats?yearMonth="+previousMonth)).andExpect(status().isOk());
    response.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetRoiStatsCollectionNotFoundForSpecifiedYearAndMonth() throws Exception {
    // input month collection does not exist, so fetch results from latest collection
    String collectionName = topRoiStatsCollectionPrefix + latestMonth;
    setupCollections(collectionName);

    String expectedJson = readJson("roiStatsFrom201705.json");
    ResultActions response = getResponseResultFor(testBaseUrl.concat("getTopRoiStats?yearMonth=201505")).andExpect(status().isOk());
    response.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetBbcForBbcId() throws Exception {
    String queryParams = buildGetGeoHierarchyQuery("BANDUNG", "roiL1", "roiL1");
    String response = getResponseFor(testBaseUrl.concat(queryParams));
    JSONArray json = (JSONArray) new JSONParser().parse(response);
    RoiLevelInfo roiInfoMO =
        JsonUtil.OBJECT_MAPPER.readValue(json.get(0).toString(), RoiLevelInfo.class);
    assertEquals("BANDUNG", roiInfoMO.getId());
  }

  @Test
  public void testGetKabupatenForBbcId() throws Exception {
    String queryParams = buildGetGeoHierarchyQuery("JABODETABEK", "roiL1", "roiL2");
    String response = getResponseFor(testBaseUrl.concat(queryParams));
    List<RoiLevelInfo> roiInfoMOs = JsonUtil.OBJECT_MAPPER.readValue(response, ArrayList.class);
    assertEquals(2, roiInfoMOs.size());
  }

  @Test
  public void testGetKecamatanForBbcId() throws Exception {
    String queryParams = buildGetGeoHierarchyQuery("JABODETABEK", "roiL1", "roiL3");
    String response = getResponseFor(testBaseUrl.concat(queryParams));
    List<RoiLevelInfo> roiInfoMOs = JsonUtil.OBJECT_MAPPER.readValue(response, ArrayList.class);
    assertEquals(3, roiInfoMOs.size());
  }

  @Test
  public void testGetKelurahanForBbcId() throws Exception {
    String queryParams = buildGetGeoHierarchyQuery("JABODETABEK", "roiL1", "roiL4");
    String response = getResponseFor(testBaseUrl.concat(queryParams));
    List<RoiLevelInfo> roiInfoMOs = JsonUtil.OBJECT_MAPPER.readValue(response, ArrayList.class);
    assertEquals(2, roiInfoMOs.size());
  }

  @Test
  public void testGetKabupatenForKabupatenId() throws Exception {
    String queryParams = buildGetGeoHierarchyQuery("3205", "roiL2", "roiL2");
    String response = getResponseFor(testBaseUrl.concat(queryParams));
    JSONArray json = (JSONArray) new JSONParser().parse(response);
    RoiLevelInfo roiInfoMO =
        JsonUtil.OBJECT_MAPPER.readValue(json.get(0).toString(), RoiLevelInfo.class);
    assertEquals("3205", roiInfoMO.getId());
  }

  @Test
  public void testGetKecamatanForKabupatenId() throws Exception {
    String queryParams = buildGetGeoHierarchyQuery("3205", "roiL2", "roiL3");
    String response = getResponseFor(testBaseUrl.concat(queryParams));
    List<RoiLevelInfo> roiInfoMOs = JsonUtil.OBJECT_MAPPER.readValue(response, ArrayList.class);
    assertEquals(2, roiInfoMOs.size());
  }

  @Test
  public void testGetKelurahanForKabupatenId() throws Exception {
    String queryParams = buildGetGeoHierarchyQuery("3206", "roiL2", "roiL4");
    String response = getResponseFor(testBaseUrl.concat(queryParams));
    List<RoiLevelInfo> roiInfoMOs = JsonUtil.OBJECT_MAPPER.readValue(response, ArrayList.class);
    assertEquals(1, roiInfoMOs.size());

  }

  @Test
  public void testGetKecamatanForKecamatanId() throws Exception {
    String queryParams = buildGetGeoHierarchyQuery("3303", "roiL3", "roiL3");
    String response = getResponseFor(testBaseUrl.concat(queryParams));
    JSONArray json = (JSONArray) new JSONParser().parse(response);
    RoiLevelInfo roiInfoMO =
        JsonUtil.OBJECT_MAPPER.readValue(json.get(0).toString(), RoiLevelInfo.class);
    assertEquals("3303", roiInfoMO.getId());
  }

  @Test
  public void testGetKelurahanForKecamatanId() throws Exception {
    String queryParams = buildGetGeoHierarchyQuery("3303", "roiL3", "roiL4");
    String response = getResponseFor(testBaseUrl.concat(queryParams));
    List<RoiLevelInfo> roiInfoMOs = JsonUtil.OBJECT_MAPPER.readValue(response, ArrayList.class);
    assertEquals(1, roiInfoMOs.size());
  }

  @Test
  public void testGetKelurahanForKelurahanId() throws Exception {
    String queryParams = buildGetGeoHierarchyQuery("3302190008", "roiL4", "roiL4");
    String response = getResponseFor(testBaseUrl.concat(queryParams));
    JSONArray json = (JSONArray) new JSONParser().parse(response);
    RoiLevelInfo roiInfoMO =
        JsonUtil.OBJECT_MAPPER.readValue(json.get(0).toString(), RoiLevelInfo.class);
    assertEquals("3302190008", roiInfoMO.getId());
  }

  @Test
  public void testGetGeoHierarchyInvalidInputRoiId() throws Exception {
    String queryParams = buildGetGeoHierarchyQuery("bbc1", "roiL1", "roiL3");
    String response = getResponseFor(testBaseUrl.concat(queryParams));
    List<RoiLevelInfo> roiInfoMOs = JsonUtil.OBJECT_MAPPER.readValue(response, ArrayList.class);
    assertEquals(0, roiInfoMOs.size());
  }

  @Test
  public void testGetGeoHierarchyEmptyInputRoiId() throws Exception {
    String queryParams = buildGetGeoHierarchyQuery("", "roiL1", "roiL3");
    ResultActions response =
        getResponseResultFor(testBaseUrl.concat(queryParams)).andExpect(status().isBadRequest());
    String content = response.andReturn().getResponse().getContentAsString();
    assertTrue(content.contains("Input parameters are null or empty."));
  }

  @Test
  public void testGetGeoHierarchyInvalidInputRoiLevel() throws Exception {
    String queryParams = buildGetGeoHierarchyQuery("bbc1", "bbcs", "roiL3");
    ResultActions response =
        getResponseResultFor(testBaseUrl.concat(queryParams)).andExpect(status().isBadRequest());
    String content = response.andReturn().getResponse().getContentAsString();
    assertTrue(content.contains("Invalid input and/or output ROI levels."));
  }

  @Test
  public void testGetGeoHierarchyEmptyInputRoiLevel() throws Exception {
    String queryParams = buildGetGeoHierarchyQuery("bbc1", "", "roiL3");
    ResultActions response =
        getResponseResultFor(testBaseUrl.concat(queryParams)).andExpect(status().isBadRequest());
    String content = response.andReturn().getResponse().getContentAsString();
    assertTrue(content.contains("Input parameters are null or empty."));
  }

  @Test
  public void testGetGeoHierarchyInvalidOutputRoiLevel() throws Exception {
    String queryParams = buildGetGeoHierarchyQuery("bbc1", "roiL3", "kelurahans");
    ResultActions response =
        getResponseResultFor(testBaseUrl.concat(queryParams)).andExpect(status().isBadRequest());
    String content = response.andReturn().getResponse().getContentAsString();
    assertTrue(content.contains("Invalid input and/or output ROI levels."));
  }

  @Test
  public void testGetGeoHierarchyEmptyOutputRoiLevel() throws Exception {
    String queryParams = buildGetGeoHierarchyQuery("bbc1", "roiL3", "");
    ResultActions response =
        getResponseResultFor(testBaseUrl.concat(queryParams)).andExpect(status().isBadRequest());
    String content = response.andReturn().getResponse().getContentAsString();
    assertTrue(content.contains("Input parameters are null or empty."));
  }

  // Negative tests for get CELL details by SITE
  @Test
  public void testMissingRoiFiltersInGetCellDetails() throws Exception {
    String json = "{\"periodType\": \"week\", "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\" } ], "
        + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("roiFilters are missing"));
  }

  @Test
  public void testNullRoiFiltersInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": null, \"periodType\": \"week\", "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\"} ], "
        + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("roiFilters are missing"));
  }

  @Test
  public void testEmptyRoiFiltersInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {}, \"periodType\": \"week\", "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\"} ], "
        + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("roiFilters are missing"));
  }

  @Test
  public void testInvalidRoiFiltersInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"abc\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\"} ], "
        + "\"sites\": [\"1_1\"], \"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Filter Key [abc] is invalid"));
  }

  @Test
  public void testMissingPeriodTypeInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\" } ], "
        + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("periodType field is invalid"));
  }

  @Test
  public void testNullPeriodTypeInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": null, "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\"} ], "
        + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("periodType field is invalid"));
  }

  @Test
  public void testEmptyPeriodTypeInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"\", "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\"} ], "
        + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("periodType field is invalid"));
  }

  @Test
  public void testInvalidPeriodTypeInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"abc\", "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\"} ], "
        + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid period type. Allowed values are week and month."));
  }

  @Test
  public void testMissingPeriodRangeInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("periodRange field is invalid"));
  }

  @Test
  public void testNullPeriodRangeInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": null, " + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("periodRange field is invalid"));
  }

  @Test
  public void testEmptyPeriodRangeInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [], " + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("periodRange field is invalid"));
  }

  @Test
  public void testInvalidPeriodRangeForWeekInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-07T00:00:00+07:00\" } ], "
        + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid period range for requested period type"));
  }

  @Test
  public void testInvalidPeriodRangeForMonthInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-31T00:00:00+07:00\"} ], "
        + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid period range for requested period type"));
  }

  @Test
  public void testMissingIndicatorsInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("indicators field is missing"));
  }

  @Test
  public void testNullIndicatorsInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"indicators\": null}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("indicators field is missing"));
  }

  @Test
  public void testEmptyIndicatorsInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"indicators\": []}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("indicators field is blank"));
  }

  @Test
  public void testInvalidUserFilterInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], "
        + "\"indicators\": [\"home\", \"sms\"], \"filterUser\" : {\"dummy.user.filter\" : [\"dummy.value\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid entries inside User Filters"));
  }

  @Test
  public void testInvalidUserFilterValueInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], "
        + "\"indicators\": [\"home\", \"sms\"], \"filterUser\" : {\"segment_unsupervised\" : [\"dummy.value\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid entries inside User Filters"));
  }

  @Test
  public void testInvalidCellFilterInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], "
        + "\"indicators\": [\"home\", \"sms\"], \"filterCell\" : {\"dummy.cell.filter\" : [\"dummy.value\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid entries inside Cell Filters"));
  }

  @Test
  public void testInvalidCellFilterValueInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], "
        + "\"indicators\": [\"home\", \"sms\"], \"filterCell\" : {\"cell_network\" : [\"dummy.value\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid entries inside Cell Filters"));
  }

  @Test
  public void testInvalidDeviceFilterInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], "
        + "\"indicators\": [\"home\", \"sms\"], \"filterDevice\" : {\"dummy.device.filter\" : [\"dummy.value\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid entries inside Device Filters"));
  }

  @Test
  public void testInvalidDeviceFilterValueInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], "
        + "\"indicators\": [\"home\", \"sms\"], \"filterDevice\" : {\"device_type\" : [\"dummy.value\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid entries inside Device Filters"));
  }

  @Test
  public void testUserTxnCollectionNotFoundInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], "
        + "\"indicators\": [\"home\", \"sms\"], \"filterDevice\" : {\"device_type\" : [\"filter.deviceType.tablet\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isInternalServerError());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Collection not found: user_txn_201701"));
  }

  @Test
  public void testCellCollectionNotFoundInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], "
        + "\"indicators\": [\"sms\"], \"filterDevice\" : {\"device_type\" : [\"filter.deviceType.tablet\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isInternalServerError());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Collection not found: user_txn_201701"));
  }

  @Test
  public void testMissingSitesInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"indicators\": [\"sms\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("sites field is invalid"));
  }

  @Test
  public void testNullSitesInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-1T00:00:00+07:00\" } ], "
        + "\"sites\": null, \"indicators\": [\"sms\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("sites field is invalid"));
  }

  @Test
  public void testEmptySitesInGetCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [], \"indicators\": [\"sms\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("sites field is invalid"));
  }

  // Negative tests for Get SITE details by ROI
  @Test
  public void testMissingRoiFiltersInGetSiteDetails() throws Exception {
    String json = "{\"periodType\": \"week\", "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\" } ], "
        + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("roiFilters are missing"));
  }

  @Test
  public void testNullRoiFiltersInGetSiteDetails() throws Exception {
    String json = "{\"roiFilters\": null, \"periodType\": \"week\", "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\" } ], "
        + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("roiFilters are missing"));
  }

  @Test
  public void testEmptyRoiFiltersInGetSiteDetails() throws Exception {
    String json = "{\"roiFilters\": {}, \"periodType\": \"week\", "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\" } ], "
        + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("roiFilters are missing"));
  }

  @Test
  public void testInvalidRoiLayerInGetSiteDetails() throws Exception {
    String json = "{\"roiFilters\": {\"abc\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], \"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Filter Key [abc] is invalid"));
  }

  @Test
  public void testMissingPeriodTypeInGetSiteDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\" } ], "
        + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("periodType field is invalid"));
  }

  @Test
  public void testNullPeriodTypeInSiteCellDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": null, "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\" } ], "
        + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("periodType field is invalid"));
  }

  @Test
  public void testEmptyPeriodTypeInGetSiteDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"\", "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\"} ], "
        + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("periodType field is invalid"));
  }

  @Test
  public void testInvalidPeriodTypeInGetSiteDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"abc\", "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\" } ], "
        + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid period type. Allowed values are week and month."));
  }

  @Test
  public void testMissingPeriodRangeInGetSiteDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("periodRange field is invalid"));
  }

  @Test
  public void testNullPeriodRangeInGetSiteDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": null, " + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("periodRange field is invalid"));
  }

  @Test
  public void testEmptyPeriodRangeInGetSiteDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [], " + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("periodRange field is invalid"));
  }

  @Test
  public void testInvalidPeriodRangeForWeekInGetSiteDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-07T00:00:00+07:00\" } ], "
        + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid period range for requested period type"));
  }

  @Test
  public void testInvalidPeriodRangeForMonthInGetSiteDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-31T00:00:00+07:00\" } ], "
        + "\"indicators\": [ \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid period range for requested period type"));
  }

  @Test
  public void testMissingIndicatorsInGetSiteDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("indicators field is missing"));
  }

  @Test
  public void testNullIndicatorsInGetSiteDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"indicators\": null}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("indicators field is missing"));
  }

  @Test
  public void testEmptyIndicatorsInGetSiteDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"indicators\": []}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("indicators field is blank"));
  }

  @Test
  public void testInvalidUserFilterInGetSiteDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], "
        + "\"indicators\": [\"home\", \"sms\"], \"filterUser\" : {\"dummy.user.filter\" : [\"dummy.value\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid entries inside User Filters"));
  }

  @Test
  public void testInvalidUserFilterValueInGetSiteDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], "
        + "\"indicators\": [\"home\", \"sms\"], \"filterUser\" : {\"segment_unsupervised\" : [\"dummy.value\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid entries inside User Filters"));
  }

  @Test
  public void testInvalidCellFilterInGetSiteDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], "
        + "\"indicators\": [\"home\", \"sms\"], \"filterCell\" : {\"dummy.cell.filter\" : [\"dummy.value\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid entries inside Cell Filters"));
  }

  @Test
  public void testInvalidCellFilterValueInGetSiteDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], "
        + "\"indicators\": [\"home\", \"sms\"], \"filterCell\" : {\"cell_network\" : [\"dummy.value\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid entries inside Cell Filters"));
  }

  @Test
  public void testInvalidDeviceFilterInGetSiteDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], "
        + "\"indicators\": [\"home\", \"sms\"], \"filterDevice\" : {\"dummy.device.filter\" : [\"dummy.value\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid entries inside Device Filters"));
  }

  @Test
  public void testInvalidDeviceFilterValueInGetSiteDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], "
        + "\"indicators\": [\"home\", \"sms\"], \"filterDevice\" : {\"device_type\" : [\"dummy.value\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid entries inside Device Filters"));
  }

  @Test
  public void testUserTxnCollectionNotFoundInGetSiteDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], "
        + "\"indicators\": [\"home\", \"sms\"], \"filterDevice\" : {\"device_type\" : [\"filter.deviceType.tablet\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isInternalServerError());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Collection not found: user_txn_201701"));
  }

  @Test
  public void testCellCollectionNotFoundInGetSiteDetails() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], "
        + "\"indicators\": [\"sms\"], \"filterDevice\" : {\"device_type\" : [\"filter.deviceType.tablet\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isInternalServerError());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Collection not found: user_txn_201701"));
  }

  @Test
  public void testAutoWarmAPISuccess() throws Exception {
    String cellCollConfDir = "index-cell_conf";
    String cellCollConfigName = "cell-config";
    String cellCollName = "cell_201701";
    String cellCollDocument = "cell_201701.xml";

    uploadCollectionConfig(cellCollConfDir, cellCollConfigName);
    createCollection(cellCollName, cellCollConfigName);
    loadDocs(cellCollDocument, cellCollName);

    String response = getResponseFor(testBaseUrl.concat("autoWarmCellSiteDetails?month=201701"));
    assertTrue(response.contains("SUCCESS"));
  }

  @Test
  public void testAutoWarmAPICollectionNotFound() throws Exception {
    ResultActions response =
        getResponseResultFor(testBaseUrl.concat("autoWarmCellSiteDetails?month=201702"));
    response.andExpect(status().isInternalServerError());
    assertTrue(response.andReturn().getResponse().getContentAsString()
        .contains("Collection not found: cell_201702"));
  }

  @Test
  public void testAutoWarmAPIInvalidMonthFormat() throws Exception {
    ResultActions response =
        getResponseResultFor(testBaseUrl.concat("autoWarmCellSiteDetails?month=20171"));
    response.andExpect(status().isInternalServerError());
    assertTrue(response.andReturn().getResponse().getContentAsString()
        .contains("month field is invalid - format : yyyymm"));
  }

  // Negative test cases for exportUsersBySites
  @Test
  public void testMissingRoiFiltersInExportUsersBySites() throws Exception {
    String json = "{\"periodType\": \"week\", "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\" } ]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("roiFilters are missing"));
  }

  @Test
  public void testNullRoiFiltersInExportUsersBySites() throws Exception {
    String json = "{\"roiFilters\": null, \"periodType\": \"week\", "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\"} ]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("roiFilters are missing"));
  }

  @Test
  public void testEmptyRoiFiltersInExportUsersBySites() throws Exception {
    String json = "{\"roiFilters\": {}, \"periodType\": \"week\", "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\"} ]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("roiFilters are missing"));
  }

  @Test
  public void testInvalidRoiLayerInExportUsersBySites() throws Exception {
    String json = "{\"roiFilters\": {\"abc\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\"} ], "
        + "\"sites\": [\"1_1\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Filter Key [abc] is invalid"));
  }

  @Test
  public void testMissingPeriodTypeInExportUsersBySites() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\" } ]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("periodType field is invalid"));
  }

  @Test
  public void testNullPeriodTypeInExportUsersBySites() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": null, "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\"} ]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("periodType field is invalid"));
  }

  @Test
  public void testEmptyPeriodTypeInExportUsersBySites() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"\", "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\"} ]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("periodType field is invalid"));
  }

  @Test
  public void testInvalidPeriodTypeInExportUsersBySites() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"abc\", "
        + "\"periodRange\": [ { \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-08T00:00:00+07:00\"} ]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid period type. Allowed values are week and month."));
  }

  @Test
  public void testMissingPeriodRangeInExportUsersBySites() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\"}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("periodRange field is invalid"));
  }

  @Test
  public void testNullPeriodRangeInExportUsersBySites() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": null}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("periodRange field is invalid"));
  }

  @Test
  public void testEmptyPeriodRangeInExportUsersBySites() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": []}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("periodRange field is invalid"));
  }

  @Test
  public void testInvalidPeriodRangeForWeekInExportUsersBySites() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-07T00:00:00+07:00\" } ]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid period range for requested period type"));
  }

  @Test
  public void testInvalidPeriodRangeForMonthInExportUsersBySites() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-01-31T00:00:00+07:00\"} ]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid period range for requested period type"));
  }

  @Test
  public void testInvalidUserFilterInExportUsersBySites() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], \"filterUser\" : {\"dummy.user.filter\" : [\"dummy.value\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid entries inside User Filters"));
  }

  @Test
  public void testInvalidUserFilterValueInExportUsersBySites() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], \"filterUser\" : {\"segment_unsupervised\" : [\"dummy.value\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid entries inside User Filters"));
  }

  @Test
  public void testInvalidCellFilterInExportUsersBySites() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], \"filterCell\" : {\"dummy.cell.filter\" : [\"dummy.value\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid entries inside Cell Filters"));
  }

  @Test
  public void testInvalidCellFilterValueInExportUsersBySites() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], \"filterCell\" : {\"cell_network\" : [\"dummy.value\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid entries inside Cell Filters"));
  }

  @Test
  public void testInvalidDeviceFilterInExportUsersBySites() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], \"filterDevice\" : {\"dummy.device.filter\" : [\"dummy.value\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid entries inside Device Filters"));
  }

  @Test
  public void testInvalidDeviceFilterValueInExportUsersBySites() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], \"filterDevice\" : {\"device_type\" : [\"dummy.value\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Invalid entries inside Device Filters"));
  }

  @Test
  public void testUserTxnCollectionNotFoundInExportUsersBySites() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], \"filterDevice\" : {\"device_type\" : [\"filter.deviceType.tablet\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isInternalServerError());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Collection not found: user_txn_201701"));
  }

  @Test
  public void testCellCollectionNotFoundInExportUsersBySites() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\"], \"filterDevice\" : {\"device_type\" : [\"filter.deviceType.tablet\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isInternalServerError());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("Collection not found: user_txn_201701"));
  }

  @Test
  public void testMissingSitesInExportUsersBySites() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("sites field is invalid"));
  }

  @Test
  public void testNullSitesInExportUsersBySites() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-1T00:00:00+07:00\" } ], "
        + "\"sites\": null}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("sites field is invalid"));
  }

  @Test
  public void testEmptySitesInExportUsersBySites() throws Exception {
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": []}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isBadRequest());
    String response = results.andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("sites field is invalid"));
  }

  @Test
  public void testGeoJsonGenerator() throws Exception {
    String expectedJson = readJson("telco-network-planning-expected.json");
    getResponseFor(testBaseUrl.concat("generateGeoJson"));
    File generatedJsonFile = new File(geoJsonGeneratedFile);
    String generatedJson = new String(Files.readAllBytes(Paths.get(generatedJsonFile.getPath())),
        Charset.forName("UTF-8"));
    generatedJsonFile.delete();
    assertEquals(expectedJson, generatedJson);
  }

  @Test
  public void testGeoHierarchyGenerator() throws Exception {
    String expectedJson = readJson("geoHierarchy-expected.json");
    new GeoHierarchyGenerator().generateHierarchy();
    File generatedJsonFile = new File("geoHierarchy.json");
    String generatedJson = new String(Files.readAllBytes(Paths.get(generatedJsonFile.getPath())),
        Charset.forName("UTF-8"));
    generatedJsonFile.delete();
    assertEquals(expectedJson, generatedJson);
  }

  private void setupCollections(String collectionName)
      throws IOException, SolrServerException, Exception {
    uploadCollectionConfig("top-roi-stats-conf", "top-roi-stats-config");
    createCollection(collectionName, "top-roi-stats-config");
    loadDocs("topRoiStatsCollection.xml", collectionName);
  }

  private String buildGetGeoHierarchyQuery(String inputRoiId, String inputRoiLevel,
      String outputRoiLevel) {
    String query = "getGeoHierarchy?inputRoiId=" + inputRoiId + "&inputRoiLevel=" + inputRoiLevel
        + "&outputRoiLevel=" + outputRoiLevel;
    return query;
  }

  private String getResponseFor(String testUrl) throws UnsupportedEncodingException, Exception {
    String responseJson = this.mockMvc.perform(get(testUrl)).andExpect(status().isOk()).andReturn()
        .getResponse().getContentAsString();

    return responseJson;
  }

  private ResultActions getResponseResultFor(String testUrl)
      throws UnsupportedEncodingException, Exception {
    ResultActions response = this.mockMvc.perform(get(testUrl));

    return response;
  }

  private void deleteAllCollections() throws IOException, SolrServerException {
    CollectionAdminRequest request = new CollectionAdminRequest.List();
    CollectionAdminResponse response =
        (CollectionAdminResponse) request.process(solrCluster.getSolrClient());
    List<String> collections = (List<String>) response.getResponse().get("collections");
    for (String collection : collections) {
      solrCluster.deleteCollection(collection);
    }
  }
}
