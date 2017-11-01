/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.web.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dataspark.spring.App;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;

/**
 * @author ragarwal, 2017-04-27
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(classes = {TelcoPlanningExportUsersAPITest.class, App.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TelcoPlanningExportUsersAPITest extends BaseSolrControllerTest {
  private String testBaseUrl = baseUrl + "/telco-network-planning/";

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
  public void testExportUsersForMonth() throws Exception {
    String expectedJson = readJson("exportedUsersForMonth.json");
    String json = "{\"roiFilters\": {\"roiL4\": [\"3603030014\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"9_9\", \"10_10\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testExportUsersForTwoWeeks() throws Exception {
    String expectedJson = readJson("exportedUsersForTwoWeeks.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" }, "
        + "{ \"startDate\": \"2017-01-09T00:00:00+07:00\", \"endDate\": \"2017-01-16T00:00:00+07:00\" } ],"
        + "\"sites\": [\"1_1\", \"2_2\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testExportUsersForOneWeekAcrossMonths() throws Exception {
    setupUserTxnCollectionsFor201702();
    String expectedJson = readJson("exportedUsersForOneWeekAcrossMonths.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-30T00:00:00+07:00\", \"endDate\": \"2017-02-06T00:00:00+07:00\"} ], "
        + "\"sites\": [\"3_3\", \"2_2\", \"1_1\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testExportUsersWithNoDeviceFilters() throws Exception {
    // Cell filters - network 2g
    // User filters - threshold data usage; threshold arpu; segment arpu very low, low, medium

    String expectedJson = readJson("exportedUsersWithNoDeviceFilters.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" } ], "
        + "\"filterCell\":{\"cell_network\":[\"filter.cellNetwork.2g\"]}"
        + ",\"filterUser\":{\"threshold_data_usage\": [0,20000],\"threshold_data_arpu\": [0,20000],"
        + "\"segment_arpu\": [\"filter.segment.arpu.veryLow\",\"filter.segment.arpu.low\",\"filter.segment.arpu.medium\"]},"
        + "\"sites\": [\"1_1\", \"2_2\",\"3_3\", \"4_4\", \"5_5\", \"6_6\",\"7_7\", \"8_8\", \"9_9\", \"10_10\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testExportUsersWithNoUserFilters() throws Exception {
    // Cell filters - network 4g, 3g
    // Device filters - network 2g.900, 3g.900; device type smart phone, feature phone, basic phone

    String expectedJson = readJson("exportedUsersWithNoUserFilters.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" } ], "
        + "\"filterCell\":{\"cell_network\":[\"filter.cellNetwork.4g\",\"filter.cellNetwork.3g\"]}"
        + ",\"filterDevice\":{\"device_frequency_2g\": [\"filter.deviceFrequency.2g.900\"],\"device_frequency_3g\": [\"filter.deviceFrequency.3g.900\"]"
        + ",\"device_type\":[\"filter.deviceType.smartphone\",\"filter.deviceType.featurephone\",\"filter.deviceType.basicphone\"]},"
        + "\"sites\": [\"1_1\", \"2_2\",\"3_3\", \"4_4\", \"5_5\", \"6_6\",\"7_7\", \"8_8\", \"9_9\", \"10_10\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testExportUsersForInvalidRoiLevelTwo() throws Exception {
    String expectedJson = readJson("exportedUsersForInvalidRoiLevelTwo.json");
    String json = "{\"roiFilters\": {\"roiL2\": [\"3600\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\"} ], "
        + "\"sites\": [\"1_1\", \"2_2\",\"3_3\", \"4_4\", \"5_5\", \"6_6\",\"7_7\", \"8_8\", \"9_9\", \"10_10\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testExportUsersForRoiLevelTwo() throws Exception {
    String expectedJson = readJson("exportedUsersForRoiLevelTwo.json");
    String json = "{\"roiFilters\": {\"roiL2\": [\"3601\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" } ], "
        + "\"sites\": [\"1_1\", \"2_2\",\"3_3\", \"4_4\", \"5_5\", \"6_6\",\"7_7\", \"8_8\", \"9_9\", \"10_10\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("exportUsersBySites"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }
}
