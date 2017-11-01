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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dataspark.spring.App;
import java.io.UnsupportedEncodingException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;

/**
 * @author ragarwal, 2017-05-05
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(classes = {TelcoPlanningGetImmediateChildrenAPITest.class, App.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TelcoPlanningGetImmediateChildrenAPITest extends BaseSolrControllerTest {
  private String testBaseUrl = baseUrl + "/telco-network-planning/";

  @Test
  public void testMissingUserNameHeader() throws Exception {
    String queryParams = buildQuery("nation,bandung", "geoJson.json");
    ResultActions response = getResponseResultFor(testBaseUrl.concat(queryParams), false, null)
        .andExpect(status().isBadRequest());
    String content = response.andReturn().getResponse().getErrorMessage();
    assertTrue(
        content.contains("Missing request header 'X-user' for method parameter of type String"));
  }

  @Test
  public void testEmptyUserNameHeader() throws Exception {
    String queryParams = buildQuery("nation,bandung", "geoJson.json");
    ResultActions response = getResponseResultFor(testBaseUrl.concat(queryParams), true, "")
        .andExpect(status().isBadRequest());
    String content = response.andReturn().getResponse().getContentAsString();
    assertEquals("Invalid userName.", content);
  }

  @Test
  public void testEmptyParentIdsHeader() throws Exception {
    String queryParams = buildQuery("", "geoJson.json");
    ResultActions response = getResponseResultFor(testBaseUrl.concat(queryParams), true, "testUser")
        .andExpect(status().isBadRequest());
    String content = response.andReturn().getResponse().getContentAsString();
    assertEquals("Invalid parentIds.", content);
  }
  
  @Test
  public void testNullParentIdsHeader() throws Exception {
    String queryParams = buildQuery(null, "geoJson.json");
    ResultActions response = getResponseResultFor(testBaseUrl.concat(queryParams), true, "testUser")
        .andExpect(status().isBadRequest());
    String content = response.andReturn().getResponse().getContentAsString();
    assertEquals("Invalid parentIds.", content);
  }
  
  @Test
  public void testEmptySourceGeoJsonHeader() throws Exception {
    String queryParams = buildQuery("nation,bandung", "");
    ResultActions response = getResponseResultFor(testBaseUrl.concat(queryParams), true, "testUser")
        .andExpect(status().isBadRequest());
    String content = response.andReturn().getResponse().getContentAsString();
    assertEquals("Invalid sourceGeoJson.", content);
  }
  
  @Test
  public void testNullSourceGeoJsonHeader() throws Exception {
    String queryParams = buildQuery("nation,bandung", null);
    ResultActions response = getResponseResultFor(testBaseUrl.concat(queryParams), true, "testUser")
        .andExpect(status().isBadRequest());
    String content = response.andReturn().getResponse().getContentAsString();
    assertEquals("Invalid sourceGeoJson.", content);
  }

  private String buildQuery(String parentIds, String sourceGeoJson) {
    String query =
        "getImmediateChildren?parentIds=" + parentIds + "&sourceGeoJson=" + sourceGeoJson;
    return query;
  }

  private ResultActions getResponseResultFor(String testUrl, boolean addUserHeader, String userName)
      throws UnsupportedEncodingException, Exception {
    HttpHeaders httpHeaders = new HttpHeaders();
    if (addUserHeader) {
      httpHeaders.add("X-user", userName);
    }
    ResultActions response = this.mockMvc.perform(get(testUrl).headers(httpHeaders));

    return response;
  }

}
