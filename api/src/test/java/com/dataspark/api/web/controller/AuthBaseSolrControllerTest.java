/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.dataspark.api.util.SolrUtils;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.cloud.MiniSolrCloudCluster;
import org.apache.solr.common.SolrInputDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author ragarwal, 2017-03-01
 */
@TestPropertySource(locations = "classpath:application.mini.auth.properties")
public class AuthBaseSolrControllerTest {
  @Autowired
  protected MiniSolrCloudCluster solrCluster;

  @Autowired
  protected WebApplicationContext wac;
  protected MockMvc mockMvc;
  protected final String baseUrl = "/v1";

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(9090);

  @Before
  public void setup() throws Exception {
    this.mockMvc = webAppContextSetup(this.wac).build();
  }

  @After
  public void tearDown() {}

  protected ResultActions getResponseResultFor(String testUrl, String json) throws Exception {
    return this.mockMvc.perform(post(testUrl).content(json).contentType(MediaType.APPLICATION_JSON)
        .accept("application/json"));
  }

  protected void uploadCollectionConfig(String confDirResource, String configName)
      throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String collectionConfDir = classLoader.getResource(confDirResource).getFile();

    solrCluster.getSolrClient().uploadConfig(FileSystems.getDefault().getPath(collectionConfDir),
        configName);
  }

  protected void createCollection(String collectionName, String configName)
      throws IOException, SolrServerException {
    solrCluster.createCollection(collectionName, 1, 1, configName, new HashMap<String, String>());
  }

  protected void loadDocs(String docsResource, String collectionName) throws Exception {
    File doc = new File(getClass().getClassLoader().getResource(docsResource).getFile());
    List<SolrInputDocument> documentList = SolrUtils.getSolrInputDocs(doc);
    solrCluster.getSolrClient().setDefaultCollection(collectionName);
    solrCluster.getSolrClient().add(documentList);
    solrCluster.getSolrClient().commit();
  }

  protected void loadParentChildDocs(String parentResource, String childResource,
      String collectionName, int numOfParentDocs) throws Exception {
    List<SolrInputDocument> parentList = SolrUtils.getSolrInputDocs(
        new File(getClass().getClassLoader().getResource(parentResource).getFile()));

    List<SolrInputDocument> childList = SolrUtils.getSolrInputDocs(
        new File(getClass().getClassLoader().getResource(childResource).getFile()));


    solrCluster.getSolrClient().setDefaultCollection(collectionName);
    for (int i = 0; i < numOfParentDocs; i++) {
      SolrInputDocument parent = parentList.get(i);
      List<SolrInputDocument> children = Arrays
          .asList(new SolrInputDocument[] {childList.get((i * 2)), childList.get((i * 2) + 1)});
      parent.addChildDocuments(children);
      solrCluster.getSolrClient().add(parent);
    }

    solrCluster.getSolrClient().commit();
  }

  protected static String readJson(String expectedJson) throws IOException {
    ClassLoader classLoader = AuthBaseSolrControllerTest.class.getClassLoader();
    String expectedJsonFile = classLoader.getResource(expectedJson).getFile();
    return new String(Files.readAllBytes(Paths.get(expectedJsonFile)), Charset.forName("UTF-8"));
  }

  protected void setupUserTxnCollectionsFor201702() throws Exception {
    String cellCollConfDir = "index-cell_conf";
    String cellCollConfigName = "cell-config";
    String cellCollName = "cell_201702";
    String cellCollDocument = "cell_201701.xml";

    String userTxnCollConfDir = "index-user_txn_conf";
    String userTxnCollConfigName = "user-txn-config";
    String userTxnCollName = "user_txn_201702";
    String userTxnParentDocument = "user_txn_201702_parent.xml";
    String userTxnChildDocument = "user_txn_201702_children.xml";

    // create cell collection
    uploadCollectionConfig(cellCollConfDir, cellCollConfigName);
    createCollection(cellCollName, cellCollConfigName);
    loadDocs(cellCollDocument, cellCollName);

    // create user txn collection
    uploadCollectionConfig(userTxnCollConfDir, userTxnCollConfigName);
    createCollection(userTxnCollName, userTxnCollConfigName);
    loadParentChildDocs(userTxnParentDocument, userTxnChildDocument, userTxnCollName, 3);
  }
}
