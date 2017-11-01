/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.spring;

import java.lang.management.ManagementFactory;
import javax.annotation.PostConstruct;
import javax.management.MBeanServer;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.management.ManagementService;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author ragarwal, 2017-03-01
 */
@SpringBootApplication
@EnableAsync
@PropertySource("classpath:api-telcoplanning.properties")
@ComponentScan(basePackages = {"com.dataspark.api"})
@Import(CachingConfig.class)
public class App extends WebMvcConfigurerAdapter {

  @Autowired
  CacheManager manager;

  @PostConstruct
  public void registerEhcache() {
    logger.info("Registering ehcache with JMX");
    MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    ManagementService.registerMBeans(manager, mBeanServer, false, false, false, true);
  }

  private static final Logger logger = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) {
    logger.info("Local Time zone detected as : " + DateTimeZone.getDefault());
    SpringApplication.run(App.class, args);
  }

}
