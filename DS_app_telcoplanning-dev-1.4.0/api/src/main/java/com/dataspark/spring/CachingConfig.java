/**
 * Copyright © DataSpark Pte Ltd 2014 - 2015. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.spring;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;

@Configuration
@PropertySource(value = {"classpath:api-telcoplanning-cache.properties"})
@EnableCaching(proxyTargetClass = true)
public class CachingConfig {

  @Bean(name = "dsTelcoApiCache")
  public CacheManager cacheManager() {
    return new EhCacheCacheManager(ehCacheCacheManager().getObject());
  }

  @Bean
  public EhCacheManagerFactoryBean ehCacheCacheManager() {
    final EhCacheManagerFactoryBean cmfb = new EhCacheManagerFactoryBean();
    cmfb.setConfigLocation(new ClassPathResource("api-telcoplanning-ehcache.xml"));
    cmfb.setShared(true);
    return cmfb;
  }
}
