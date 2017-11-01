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
 * @author ashutosh, 2017-03-10
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(classes = {TelcoPlanningSiteDetailsAPITest.class, App.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TelcoPlanningSiteDetailsAPITest extends BaseSolrControllerTest {
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
    loadParentChildDocs(userTxnParentDocument, userTxnChildDocument, userTxnCollName, 11);
  }

  @Test
  public void testGetSiteDetailsReturnPayloadRevenueForMonth() throws Exception {
    String expectedJson = readJson("siteDetailsWithPayloadRevenueForMonth.json");
    String json = "{\"roiFilters\": {\"roiL4\": [\"3603030014\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"indicators\": [\"sms\", \"calls\", \"home\", \"payload\", \"revenue\", \"total_stay_duration\",\"avg_stay_duration\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetSitesDetailsNoPayloadRevenueForWeek() throws Exception {
    String expectedJson = readJson("siteDetailsNoPayloadRevenueForWeek.json");
    String json = "{\"roiFilters\": {\"roiL4\": [\"3603030014\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" } ], "
        + "\"indicators\": [\"sms\", \"calls\", \"home\", \"payload\", \"revenue\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetSiteDetailsWithNoDeviceFilters() throws Exception {
    // Cell filters - network 2g
    // User filters - threshold data usage; threshold arpu; segment arpu very low, low, medium

    String expectedJson = readJson("siteDetailsWithNoDeviceFilters.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" } ], "
        + "\"indicators\": [\"sms\", \"home\"], \"filterCell\":{\"cell_network\":[\"filter.cellNetwork.2g\"]}"
        + ",\"filterUser\":{\"threshold_data_usage\": [0,20000],\"threshold_data_arpu\": [0,20000],"
        + "\"segment_arpu\": [\"filter.segment.arpu.veryLow\",\"filter.segment.arpu.low\",\"filter.segment.arpu.medium\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetSiteDetailsWithNoUserFilters() throws Exception {
    // Cell filters - network 4g, 3g
    // Device filters - network 2g.900, 3g.900; device type smart phone, feature phone, basic phone

    String expectedJson = readJson("siteDetailsWithNoUserFilters.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" } ], "
        + "\"indicators\": [\"sms\", \"home\"], \"filterCell\":{\"cell_network\":[\"filter.cellNetwork.4g\",\"filter.cellNetwork.3g\"]}"
        + ",\"filterDevice\":{\"device_frequency_2g\": [\"filter.deviceFrequency.2g.900\"],\"device_frequency_3g\": [\"filter.deviceFrequency.3g.900\"]"
        + ",\"device_type\":[\"filter.deviceType.smartphone\",\"filter.deviceType.featurephone\",\"filter.deviceType.basicphone\"]}}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetSiteDetailsWithNoFilters() throws Exception {
    String expectedJson = readJson("siteDetailsWithNoFilters.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" } ], "
        + "\"indicators\": [\"sms\", \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetSiteDetailsWithNoPayloadRevenueIndicators() throws Exception {
    String expectedJson = readJson("siteDetailsWithNoPayloadRevenueIndicators.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" }], "
        + "\"indicators\": [\"home\", \"work\", \"calls\", \"sms\",\"unique_people\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetSiteDetailsWithNoCellStatsIndicators() throws Exception {
    String expectedJson = readJson("siteDetailsWithNoCellStatsIndicators.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" }], "
        + "\"indicators\": [\"home\", \"work\", \"payload\",\"revenue\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetSiteDetailsWithNoHomeWorkIndicators() throws Exception {
    String expectedJson = readJson("siteDetailsWithNoHomeWorkIndicators.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" }], "
        + "\"indicators\": [\"calls\", \"sms\",\"unique_people\", \"payload\",\"revenue\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetSiteDetailsWithOneIndicatorFromEachGroup() throws Exception {
    String expectedJson = readJson("siteDetailsWithOneIndicatorFromEachGroup.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" }], "
        + "\"indicators\": [\"work\", \"unique_people\", \"revenue\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetSiteDetailsWithAllIndicators() throws Exception {
    String expectedJson = readJson("siteDetailsWithAllIndicators.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" }], "
        + "\"indicators\": [\"work\", \"home\",\"calls\", \"sms\",\"unique_people\", \"payload\",\"revenue\",\"total_stay_duration\",\"avg_stay_duration\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetSiteDetailsForOneMonth() throws Exception {
    String expectedJson = readJson("siteDetailsForOneMonth.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"indicators\": [\"calls\", \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isOk());
    System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetSiteDetailsForTwoWeeks() throws Exception {
    String expectedJson = readJson("siteDetailsForTwoWeeks.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" }, "
        + "{ \"startDate\": \"2017-01-09T00:00:00+07:00\", \"endDate\": \"2017-01-16T00:00:00+07:00\" } ], "
        + "\"indicators\": [\"home\",\"call\",\"total_stay_duration\",\"avg_stay_duration\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isOk());
    System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetSiteDetailsForOneWeekAcrossMonths() throws Exception {
    setupUserTxnCollectionsFor201702();
    String expectedJson = readJson("siteDetailsForOneWeekAcrossMonths.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-30T00:00:00+07:00\", \"endDate\": \"2017-02-06T00:00:00+07:00\"} ], "
        + "\"indicators\": [\"sms\", \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());

    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetSiteDetailsForInvalidRoiLevelFour() throws Exception {
    String expectedJson = readJson("siteDetailsForInvalidRoiLevelFour.json");
    String json = "{\"roiFilters\": {\"roiL4\": [\"3603030031\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\":  \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" } ], "
        + "\"indicators\": [\"sms\", \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetSiteDetailsForRoiLevelFour() throws Exception {
    String expectedJson = readJson("siteDetailsForvalidRoiLevelFour.json");
    String json = "{\"roiFilters\": {\"roiL4\": [\"3603030011\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\":  \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" } ], "
        + "\"indicators\": [\"sms\", \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetSiteDetailsForInvalidRoiLevelThree() throws Exception {
    String expectedJson = readJson("siteDetailsForInvalidRoiLevelThree.json");
    String json = "{\"roiFilters\": {\"roiL3\": [\"3603030\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\"} ], "
        + "\"indicators\": [\"sms\", \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testSiteDetailsForRoiLevelThree() throws Exception {
    String expectedJson = readJson("siteDetailsForRoiLevelThree.json");
    String json = "{\"roiFilters\": {\"roiL3\": [\"3603032\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\"} ], "
        + "\"indicators\": [\"calls\", \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetSiteDetailsForInvalidRoiLevelTwo() throws Exception {
    String expectedJson = readJson("siteDetailsForInvalidRoiLevelTwo.json");
    String json = "{\"roiFilters\": {\"roiL2\": [\"3600\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\"} ], "
        + "\"indicators\": [\"sms\", \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetSiteDetailsForRoiLevelTwo() throws Exception {
    String expectedJson = readJson("siteDetailsForRoiLevelTwo.json");
    String json = "{\"roiFilters\": {\"roiL2\": [\"3601\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" } ], "
        + "\"indicators\": [\"sms\", \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetSiteDetailsWithHomeSitesFilter() throws Exception {
    String expectedJson = readJson("siteDetailsWithHomeSitesFilter.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" } ], "
        + "\"filterUser\":{\"home_location\": [\"roiL2:4171\"]},"
        + "\"indicators\": [\"sms\", \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetSiteDetailsWithWorkSitesFilter() throws Exception {
    String expectedJson = readJson("siteDetailsWithWorkSitesFilter.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"JABODETABEK\"]}, \"periodType\": \"week\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-02T00:00:00+07:00\", \"endDate\": \"2017-01-09T00:00:00+07:00\" } ], "
        + "\"filterUser\":{\"work_location\": [\"roiL3:4671011\"]},"
        + "\"indicators\": [\"sms\", \"home\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testGetSiteDetailsWithVqiCongestion() throws Exception {
    String expectedJson = readJson("siteDetailsWithVqiCongestion.json");
    String json = "{\"roiFilters\": {\"roiL1\": [\"BANDUNG\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"indicators\": [\"vqi\", \"congestion\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getSiteDetailsByROI"), json)
        .andExpect(status().isOk());
    // System.out.println(results.andReturn().getResponse().getContentAsString());
    results.andExpect(content().json(expectedJson, true));
  }
}
