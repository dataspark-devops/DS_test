package com.dataspark.api.web.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dataspark.spring.App;
import java.text.SimpleDateFormat;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;

/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */

/**
 * @author ragarwal, 2017-03-10
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(classes = {TelcoPlanningCellDetailsAPITest.class, App.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TelcoPlanningCellDetailsAPITest extends BaseSolrControllerTest {
  private String testBaseUrl = baseUrl + "/telco-network-planning/";
  public static SimpleDateFormat sdf = new SimpleDateFormat("YYYYMM");

  @Test
  public void setupCollections() throws Exception {
    String cellCollConfDir = "index-cell_conf";
    String cellCollConfigName = "cell-config";
    String cellCollName = "cell_201701";
    String cellCollDocument = "cell_201701.xml";

    String userTxnCollConfDir = "index-user_txn_conf";
    String userTxnCollConfigName = "user-txn-config";
    String userTxnCollName = "user_txn_201701";
    String userTxnParentDocument = "user_txn_201701_parent.xml";
    String userTxnChildDocument = "user_txn_201701_children.xml";

    // create cell collection
    uploadCollectionConfig(cellCollConfDir, cellCollConfigName);
    createCollection(cellCollName, cellCollConfigName);
    loadDocs(cellCollDocument, cellCollName);

    // create user txn collection
    uploadCollectionConfig(userTxnCollConfDir, userTxnCollConfigName);
    createCollection(userTxnCollName, userTxnCollConfigName);
    loadParentChildDocs(userTxnParentDocument, userTxnChildDocument, userTxnCollName, 10);
  }

  @Test
  public void testGetCellDetailsReturnPayloadRevenueForMonth() throws Exception {
    String expectedJson = readJson("cellDetailsWithPayloadRevenueForMonth.json");
    String json = "{\"roiFilters\": {\"roiL4\": [\"3603030014\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"9_9\", \"10_10\"], \"indicators\": [\"sms\", \"calls\", \"home\", \"payload\", \"revenue\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetCellDetailsNoPayloadRevenueForWeek() throws Exception {
    String expectedJson = readJson("cellDetailsNoPayloadRevenueForWeek.json");
    String json = "{\"roiFilters\": {\"roiL4\": [\"3603030014\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" } ], "
        + "\"sites\": [\"9_9\", \"10_10\"], \"indicators\": [\"sms\", \"calls\", \"home\", \"payload\", \"revenue\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetCellDetailsWithNoDeviceFilters() throws Exception {
    // Cell filters - network 2g
    // User filters - threshold data usage; threshold arpu; segment arpu very low, low, medium

    String expectedJson = readJson("cellDetailsWithNoDeviceFilters.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\", \"2_2\",\"3_3\", \"4_4\", \"5_5\", \"6_6\",\"7_7\", \"8_8\", \"9_9\", \"10_10\"], "
        + "\"indicators\": [\"sms\", \"home\"], \"filterCell\":{\"cell_network\":[\"filter.cellNetwork.2g\"]}"
        + ",\"filterUser\":{\"threshold_data_usage\": [100,20000],\"threshold_data_arpu\": [0,20000],"
        + "\"segment_arpu\": [\"filter.segment.arpu.veryLow\",\"filter.segment.arpu.low\",\"filter.segment.arpu.medium\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetCellDetailsWithNoUserFilters() throws Exception {
    // Cell filters - network 4g, 3g
    // Device filters - network 2g.900, 3g.900; device type smart phone, feature phone, basic phone

    String expectedJson = readJson("cellDetailsWithNoUserFilters.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\", \"2_2\",\"3_3\", \"4_4\", \"5_5\", \"6_6\",\"7_7\", \"8_8\", \"9_9\", \"10_10\"], "
        + "\"indicators\": [\"calls\", \"work\", \"revenue\"], \"filterCell\":{\"cell_network\":[\"filter.cellNetwork.4g\",\"filter.cellNetwork.3g\"]}"
        + ",\"filterDevice\":{\"device_frequency_2g\": [\"filter.deviceFrequency.2g.900\"],\"device_frequency_3g\": [\"filter.deviceFrequency.3g.900\"]"
        + ",\"device_type\":[\"filter.deviceType.smartphone\",\"filter.deviceType.featurephone\",\"filter.deviceType.basicphone\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetCellDetailsWithNoFilters() throws Exception {
    String expectedJson = readJson("cellDetailsWithNoFilters.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"sites\": [\"1_1\", \"2_2\",\"3_3\", \"4_4\", \"5_5\", \"6_6\",\"7_7\", \"8_8\", \"9_9\", \"10_10\"], "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" } ], "
        + "\"indicators\": [\"sms\", \"home\", \"revenue\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetCellDetailsWithNoPayloadRevenueIndicators() throws Exception {
    String expectedJson = readJson("cellDetailsWithNoPayloadRevenueIndicators.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\"}], "
        + "\"sites\": [\"1_1\", \"8_8\", \"9_9\", \"10_10\"], "
        + "\"indicators\": [\"home\", \"work\", \"calls\", \"sms\",\"unique_people\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetCellDetailsWithNoCellStatsIndicators() throws Exception {
    String expectedJson = readJson("cellDetailsWithNoCellStatsIndicators.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" }], "
        + "\"sites\": [\"3_3\", \"5_5\", \"9_9\", \"10_10\"], "
        + "\"indicators\": [\"home\", \"work\", \"payload\",\"revenue\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetCellDetailsWithNoHomeWorkIndicators() throws Exception {
    String expectedJson = readJson("cellDetailsWithNoHomeWorkIndicators.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{\"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" }], "
        + "\"sites\": [\"3_3\", \"5_5\", \"9_9\", \"10_10\"], "
        + "\"indicators\": [\"calls\", \"sms\",\"unique_people\", \"payload\",\"revenue\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetCellDetailsWithOneIndicatorFromEachGroup() throws Exception {
    String expectedJson = readJson("cellDetailsWithOneIndicatorFromEachGroup.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" }], "
        + "\"sites\": [\"2_2\", \"3_3\", \"8_8\"], "
        + "\"indicators\": [\"work\", \"unique_people\", \"revenue\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetCellDetailsWithAllIndicators() throws Exception {
    String expectedJson = readJson("cellDetailsWithAllIndicators.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" }], "
        + "\"sites\": [\"2_2\", \"3_3\", \"6_6\", \"7_7\"], "
        + "\"indicators\": [\"work\", \"home\",\"calls\", \"sms\",\"unique_people\", \"payload\",\"revenue\", \"total_stay_duration\", \"avg_stay_duration\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetCellDetailsForOneMonth() throws Exception {
    String expectedJson = readJson("cellDetailsForOneMonth.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\"}], "
        + "\"sites\": [\"1_1\", \"2_2\",\"3_3\", \"4_4\", \"5_5\", \"6_6\", \"7_7\", \"8_8\", \"9_9\",\"10_10\"], "
        + "\"indicators\": [\"work\", \"home\",\"calls\", \"sms\",\"unique_people\", \"payload\",\"revenue\", \"total_stay_duration\", \"avg_stay_duration\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetCellDetailsForTwoWeeks() throws Exception {
    String expectedJson = readJson("cellDetailsForTwoWeeks.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02\", \"endDate\": \"2017-01-09\" }, "
        + "{ \"startDate\": \"2017-01-09T00:00:00+07:00\", \"endDate\": \"2017-01-16T00:00:00+07:00\"} ], "
        + "\"sites\": [\"1_1\", \"4_4\", \"5_5\", \"9_9\"], "
        + "\"indicators\": [\"work\", \"home\",\"calls\", \"sms\",\"unique_people\", \"payload\",\"revenue\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetCellDetailsForOneWeekAcrossMonths() throws Exception {
    setupUserTxnCollectionsFor201702();
    String expectedJson = readJson("cellDetailsForOneWeekAcrossMonths.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-30T00:00:00+07:00\", \"endDate\": \"2017-02-06T00:00:00+07:00\"} ], "
        + "\"sites\": [\"1_1\", \"2_2\", \"3_3\"], \"indicators\": [\"sms\", \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetCellDetailsForInvalidRoiLevelFour() throws Exception {
    String expectedJson = readJson("cellDetailsForInvalidRoiLevelFour.json");
    String json = "{\"roiFilters\": {\"roiL4\": [\"3603030010\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\":  \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" } ], "
        + "\"sites\": [\"9_9\", \"10_10\"], \"indicators\": [\"sms\", \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isOk());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetCellDetailsForRoiLevelFour() throws Exception {
    String expectedJson = readJson("cellDetailsForRoiLevelFour.json");
    String json = "{\"roiFilters\": {\"roiL4\": [\"3603030014\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" } ], "
        + "\"sites\": [\"9_9\", \"10_10\"], \"indicators\": [\"calls\", \"home\", \"total_stay_duration\", \"avg_stay_duration\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetCellDetailsForInvalidRoiLevelThree() throws Exception {
    String expectedJson = readJson("cellDetailsForInvalidRoiLevelThree.json");
    String json = "{\"roiFilters\": {\"roiL3\": [\"3603030\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\"} ], "
        + "\"sites\": [\"3_3\", \"4_4\"], \"indicators\": [\"sms\", \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isOk());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetCellDetailsForRoiLevelThree() throws Exception {
    String expectedJson = readJson("cellDetailsForRoiLevelThree.json");
    String json = "{\"roiFilters\": {\"roiL3\": [\"3603032\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\"} ], "
        + "\"sites\": [\"3_3\", \"4_4\"], \"indicators\": [\"calls\", \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetCellDetailsForInvalidRoiLevelTwo() throws Exception {
    String expectedJson = readJson("cellDetailsForInvalidRoiLevelTwo.json");
    String json = "{\"roiFilters\": {\"roiL2\": [\"3600\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\"} ], "
        + "\"sites\": [\"1_1\", \"2_2\"], \"indicators\": [\"sms\", \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isOk());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetCellDetailsForRoiLevelTwo() throws Exception {
    String expectedJson = readJson("cellDetailsForRoiLevelTwo.json");
    String json = "{\"roiFilters\": {\"roiL2\": [\"3601\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\", \"2_2\"], \"indicators\": [\"sms\", \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetCellDetailsWithHomeSitesFilter() throws Exception {
    String expectedJson = readJson("cellDetailsWithHomeSitesFilter.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" } ], "
        + "\"filterUser\":{\"home_location\": [\"roiL1:JABODETABEK\"]},"
        + "\"sites\": [\"1_1\", \"2_2\"], \"indicators\": [\"sms\", \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetCellDetailsWithWorkSitesFilter() throws Exception {
    String expectedJson = readJson("cellDetailsWithWorkSitesFilter.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" } ], "
        + "\"filterUser\":{\"work_location\": [\"roiL1:JABODETABEK\"]},"
        + "\"sites\": [\"6_6\", \"7_7\"], \"indicators\": [\"sms\", \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

}
