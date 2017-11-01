package com.dataspark.api.web.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dataspark.api.data.RoiNode;
import com.dataspark.api.invoker.AuthSquareConstants;
import com.dataspark.api.solr.util.JsonUtil;
import com.dataspark.spring.App;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
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
@ContextConfiguration(classes = {TelcoPlanningAuthSquareBasedTest.class, App.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TelcoPlanningAuthSquareBasedTest extends AuthBaseSolrControllerTest {
  private String testBaseUrl = baseUrl + "/telco-network-planning/";
  public static SimpleDateFormat sdf = new SimpleDateFormat("YYYYMM");

  @Value("${collection.toproistats.prefix:top_roi_stats_}")
  private String topRoiStatsCollectionPrefix;

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
  public void testPostRequestInterceptorAuthorized() throws Exception {
    String expectedJson = readJson("cellDetailsWithPayloadRevenueForMonth.json");
    createMockAuthServer("AUTHORIZED");
    String json = "{\"roiFilters\": {\"roiL4\": [\"3603030014\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"9_9\", \"10_10\"], \"indicators\": [\"sms\", \"calls\", \"home\", \"payload\", \"revenue\"]}";

    ResultActions results = getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().isOk());
    results.andExpect(content().json(expectedJson, true));
  }

  @Test
  public void testPostRequestInterceptorUnauthorized() throws Exception {
    createMockAuthServer("UNAUTHORIZED");
    String json = "{\"roiFilters\": {\"roiL4\": [\"3603030014\"]}, \"periodType\": \"month\", "
        + "\"periodRange\": [{ \"startDate\": \"2017-01-01T00:00:00+07:00\", \"endDate\": \"2017-02-01T00:00:00+07:00\" } ], "
        + "\"sites\": [\"9_9\", \"10_10\"], \"indicators\": [\"sms\", \"calls\", \"home\", \"payload\", \"revenue\"]}";
    getResponseResultFor(testBaseUrl.concat("getCellDetailsBySites"), json)
        .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
  }

  @Test
  public void testGetRequestInterceptor() throws Exception {
    createMockFilterKeysServer();
    String collectionName = topRoiStatsCollectionPrefix + "201504";
    uploadCollectionConfig("top-roi-stats-conf", "top-roi-stats-config");
    createCollection(collectionName, "top-roi-stats-config");
    loadDocs("topRoiStatsCollection.xml", collectionName);

    List<Map<String, Object>> roiStatsMO = new ArrayList<Map<String, Object>>();
    String response = getResponseFor(testBaseUrl.concat("getTopRoiStats?yearMonth=201504"));
    JSONObject json = (JSONObject) new JSONParser().parse(response);
    roiStatsMO = JsonUtil.OBJECT_MAPPER.convertValue(json.get("results"), roiStatsMO.getClass());
    assertEquals(2, roiStatsMO.size());
  }

  @Test
  public void testGetImmediateChildren() throws Exception {
    createMockGetImmediateChildrenResponse();
    String response = getResponseFor(testBaseUrl
        .concat("getImmediateChildren?parentIds=nation&sourceGeoJson=telco-network-planning.json"));
    List<RoiNode> roiNodeMOs = new ArrayList<RoiNode>();
    JSONArray json = (JSONArray) new JSONParser().parse(response);
    roiNodeMOs = JsonUtil.OBJECT_MAPPER.convertValue(json, roiNodeMOs.getClass());
    RoiNode roiNode = JsonUtil.OBJECT_MAPPER.convertValue(json.get(0), RoiNode.class);
    assertEquals(1, roiNodeMOs.size());
    assertEquals("BANYUMAS_PURWOKERTO_CILACAP", roiNode.getId());
  }

  public void createMockFilterKeysServer() throws JsonProcessingException {
    HashMap<String, Set<String>> filteredKeys = new HashMap<String, Set<String>>();
    Set<String> keys = new HashSet<String>();
    keys.add("KEDIRI");
    keys.add("KUTAI_KARTANEGARA");
    filteredKeys.put(AuthSquareConstants.Settings.ROI_VALUE, keys);
    stubFor(post(urlEqualTo("/authadmin/filterKeysBasedOnPerms")).willReturn(
        aResponse().withStatus(200).withHeader("Content-Type", APPLICATION_JSON.toString()).but()
            .withBody(JsonUtil.OBJECT_MAPPER.writeValueAsString(filteredKeys))));
  }

  public void createMockGetImmediateChildrenResponse() {
    stubFor(get(urlEqualTo(
        "/authadmin/geoHierarchy/getImmediateChildren?parentIds=nation&sourceGeoJson=telco-network-planning.json"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", APPLICATION_JSON.toString()).but().withBody(
                    "[  {\"id\": \"BANYUMAS_PURWOKERTO_CILACAP\",\"label\": \"SITES:BANYUMAS PURWOKERTO CILACAP\","
                        + "\"displayType\": \"sites\",\"aggregatableAt\": \"roiL2\",\"nodeValueIsA\": \"bbc\",\"roiFilters\": "
                        + "{  \"roiL1\": [\"BANYUMAS_PURWOKERTO_CILACAP\"  ]},\"children\": []  }]")));
  }

  public void createMockAuthServer(String response) {
    stubFor(post(urlEqualTo("/authadmin/authorizeKeys")).willReturn(aResponse().withStatus(200)
        .withHeader("Content-Type", APPLICATION_JSON.toString()).withBody(response)));

  }

  private String getResponseFor(String testUrl) throws UnsupportedEncodingException, Exception {
    String responseJson = this.mockMvc.perform(get(testUrl).header("X-user", "testUser"))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

    return responseJson;
  }

}
