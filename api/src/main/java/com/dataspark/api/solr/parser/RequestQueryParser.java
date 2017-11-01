/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.solr.parser;

import com.dataspark.api.exception.ServiceException;
import com.dataspark.api.service.SolrToJsonPropMappingService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.extern.log4j.Log4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author ragarwal, ashutosh
 *
 *         2017-03-01
 */
@Service
@Log4j
public class RequestQueryParser {

  @Autowired
  private JsonFacetParser jsonFacetUtils;

  @Autowired
  private SolrToJsonPropMappingService solrToJsonPropSvc;

  @Value("${solr.block.join.query.template}")
  private String solrBlockJoinQueryTemplate;

  @Value("${collection.id.field.name}")
  private String collectionIdFieldName;

  @Value("${export.user.max.count}")
  private Integer maxUsersCount;

  @Value("${cell.max.count}")
  private Integer maxCellsCount;

  public SolrParams getTopRoiStatsQuery(String collectionName) {
    ModifiableSolrParams query = getTelcoPlanningQueryTemplate(collectionName, 40,
        new ArrayList<String>(solrToJsonPropSvc.roiStatsCollInfoMap.keySet()));
    log.info("Solr query: " + query);
    return query;
  }

  public SolrParams getSiteDetailsQuery(String collectionName, Set<String> filters,
      List<String> fieldList) {
    ModifiableSolrParams query =
        getTelcoPlanningQueryTemplate(collectionName, maxCellsCount, fieldList);
    for (String filter : filters) {
      query.add("fq", filter);
    }
    log.info("Solr query for getSiteDetails: " + query);
    return query;
  }


  private ModifiableSolrParams getTelcoPlanningQueryTemplate(String collectionName, Integer rows,
      List<String> fieldList) {
    final ModifiableSolrParams solrQuery = getBaseQueryTemplate();
    solrQuery.set("collection", collectionName);
    solrQuery.set("rows", rows);
    if (fieldList != null) {
      String fl = fieldList.toString().replace("[", "").replace("]", "");
      solrQuery.add("fl", fl);
    }
    return solrQuery;
  }

  private static ModifiableSolrParams getBaseQueryTemplate() {
    String rowsCount = "0";
    final ModifiableSolrParams solrQuery = new ModifiableSolrParams();

    solrQuery.set("qt", "/select");
    solrQuery.set("q", "*:*");
    solrQuery.set("wt", "json");
    solrQuery.set("collection", "base-collection");
    solrQuery.set("facet", true);
    solrQuery.set("rows", rowsCount);
    return solrQuery;
  }

  public SolrParams getHomeWorkDetailsQuery(Set<String> parentFilters, Set<String> childFilters,
      String collectionName, List<String> requestedIndicators) throws ServiceException {
    ModifiableSolrParams solrQuery = getTelcoPlanningQueryTemplate(collectionName, 0, null);

    solrQuery.set("q", buildSolrBlockJoinQuery(childFilters, collectionIdFieldName + ":*", true));
    solrQuery.set("json.facet",
        jsonFacetUtils.buildJsonFacetForHomeWorkResults(requestedIndicators));

    if (!CollectionUtils.isEmpty(parentFilters)) {
      for (String pFilter : parentFilters) {
        solrQuery.add("fq", pFilter);
      }
    }

    log.info("Solr query for getHomeWorkDetails : " + solrQuery);
    return solrQuery;
  }


  public SolrParams exportUsersPerSitesQuery(Set<String> parentFilters, Set<String> childFilters,
      String collectionName, List<String> fieldList) throws ServiceException {

    ModifiableSolrParams query =
        getTelcoPlanningQueryTemplate(collectionName, maxUsersCount, fieldList);
    query.set("q", buildSolrBlockJoinQuery(childFilters, collectionIdFieldName + ":*", true));
    if (!CollectionUtils.isEmpty(parentFilters)) {
      for (String pFilter : parentFilters) {
        query.add("fq", pFilter);
      }
    }
    log.info("Solr query for exportUsersPerSitesQuery : " + query);
    return query;
  }

  public SolrParams getCellStatsQuery(Set<String> parentFilters, Set<String> childFilters,
      String collectionName, List<String> requestedIndicators) throws ServiceException {
    ModifiableSolrParams solrQuery = getTelcoPlanningQueryTemplate(collectionName, 0, null);

    solrQuery.set("q", buildSolrBlockJoinQuery(parentFilters, collectionIdFieldName + ":*", false));
    solrQuery.set("json.facet",
        jsonFacetUtils.buildJsonFacetForCellStats(collectionIdFieldName, requestedIndicators));

    if (!CollectionUtils.isEmpty(childFilters)) {
      for (String filter : childFilters) {
        solrQuery.add("fq", filter);
      }
    }

    log.info("Solr query for getCellStats: " + solrQuery);
    return solrQuery;
  }


  public SolrParams getSiteStatsDetailsQuery(Set<String> parentFilters, Set<String> childFilters,
      String collectionName, List<String> requestedIndicators) throws ServiceException {
    ModifiableSolrParams solrQuery = getTelcoPlanningQueryTemplate(collectionName, 0, null);

    solrQuery.set("q", buildSolrBlockJoinQuery(parentFilters, collectionIdFieldName + ":*", false));
    solrQuery.set("json.facet",
        jsonFacetUtils.buildJsonFacetForSiteStats(collectionIdFieldName, requestedIndicators));

    if (!CollectionUtils.isEmpty(childFilters)) {
      for (String filter : childFilters) {
        solrQuery.add("fq", filter);
      }
    }

    log.info("Solr query for getCellStatsDetails: " + solrQuery);
    return solrQuery;
  }


  private String buildSolrBlockJoinQuery(Set<String> otherFilters, String defaultParentFilter,
      boolean fetchParent) throws ServiceException {
    String query = solrBlockJoinQueryTemplate;
    if (StringUtils.isEmpty(defaultParentFilter)) {
      throw new ServiceException("Parent filter is null or empty.");
    }
    String otherFiltersStr = (CollectionUtils.isEmpty(otherFilters)) ? defaultParentFilter
        : StringUtils.join(otherFilters, " AND ");

    query = query.replace("parentFilter", defaultParentFilter);
    query = query.replace("otherFilters", otherFiltersStr);

    query = fetchParent ? query.replace("parentOrChild", "parent which")
        : query.replace("parentOrChild", "child of");

    log.info("Solr block join query: " + query);
    return query;
  }

}
