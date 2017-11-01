/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.solr.repository;

import javax.annotation.Resource;
import lombok.extern.log4j.Log4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author ragarwal
 *
 * 2017-03-01
 */
@Configuration
@EnableAutoConfiguration
@Log4j
public class SolrConnect {

  @Resource
  private Environment env;

  @Bean(name = "cloudSolrServer")
  public SolrClient cloudSolrServer(@Value("${solr.zkHost}") final String solrzkHost) {
    CloudSolrClient cloudClient = new CloudSolrClient(solrzkHost, true);
    cloudClient.connect();
    log.info("Connected to Solr Cloud!");
    return cloudClient;
  }
}
