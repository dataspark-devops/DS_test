/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.service;

import static java.util.Collections.unmodifiableMap;

import com.dataspark.api.solr.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Log4j
public class FilterService {

  private static final String TO = "to";
  private static final String FROM = "from";
  private static final String MAPPING = "mapping";
  private static final String SOLR_FIELD = "solrField";
  private static final String NAME = "name";
  private static final String TYPE = "type";

  private static final String MATCHALL_REGEX = "*";

  public final Map<String, String> fieldMap;
  public final Map<String, String> typeMap;

  public final Map<String, Map<String, String>> valueMap;
  public final Map<String, Map<String, String>> inverseValueMap;

  private FilterService(@Value("${telcoplanning.filtermapping.solr}") String mappingFile) {

    Map<String, String> fieldMap = new HashMap<>();
    Map<String, String> typeMap = new HashMap<>();

    Map<String, Map<String, String>> valueMap = new HashMap<>();
    Map<String, Map<String, String>> inverseValueMap = new HashMap<>();


    try {
      InputStream stream = new FileInputStream(new File(mappingFile));
      JsonNode map = JsonUtil.OBJECT_MAPPER.readTree(stream);

      JsonNode pf = map.get("Filters");

      for (JsonNode node : pf) {
        JsonNode name = node.get(NAME);

        fieldMap.put(name.asText(), node.get(SOLR_FIELD).asText());
        if (node.get(TYPE) != null) {
          typeMap.put(name.asText(), node.get(TYPE).asText());
        }

        Map<String, String> valMap = new HashMap<>();
        Map<String, String> invValMap = new HashMap<>();

        for (JsonNode val : node.get(MAPPING)) {

          JsonNode from = val.get(FROM);
          JsonNode to = val.get(TO);

          if (from == null || to == null) {
            log.error(
                "Input file format is wrong for the field " + name + ". Will try to recover.");
            continue;
          }
          valMap.put(from.asText(), to.asText());
          invValMap.put(to.asText(), from.asText());

        }
        valueMap.put(name.asText(), valMap);
        inverseValueMap.put(node.get(SOLR_FIELD).asText(), invValMap);

      }
      stream.close();
    } catch (IOException e) {
      log.error("Failed to start up filter service");
      throw new RuntimeException("Failed to startup Filter Service: ", e);
    }
    this.fieldMap = unmodifiableMap(fieldMap);
    this.typeMap = unmodifiableMap(typeMap);
    this.valueMap = unmodifiableMap(valueMap);
    this.inverseValueMap = unmodifiableMap(inverseValueMap);
  }


  public boolean validateFilters(Map<String, List<String>> filters) {

    /*
     * For all the filters requested in the request, perform a two step validation. Step 1 of the
     * validation is check whether the requested filter has an associated mapping defined Step 2 of
     * the validation is that once the requested filter is known to have a some definition in the
     * mapping, check wether the requested value(in the array) has a defined value mapping
     */
    for (Entry<String, List<String>> entry : filters.entrySet()) {
      for (String element : entry.getValue()) {

        // STEP 1 : For each filter profile see if a mapping of possible values has been defined in
        // the
        // filteringjson
        if (!valueMap.containsKey(entry.getKey())) {
          return false;
        }

        // If the value map defines a * = * pass through, then all values are permissible
        if (valueMap.get(entry.getKey()).containsKey(MATCHALL_REGEX)
            && valueMap.get(entry.getKey()).get(MATCHALL_REGEX).equals(MATCHALL_REGEX)) {
          log.info("Pass through defined for element : " + entry.getKey() + " with value(s) : "
              + entry.getValue() + ". Skipping this filter element validation.");
          continue;
        }

        // if the execution has reached here, that means no pass through has been defined, in which
        // case check wether the value mapping is defined or not
        if (!valueMap.get(entry.getKey()).containsKey(element)) {
          return false;
        }


      }
    }
    return true;
  }

  public boolean validateFacets(List<String> profileFacets) {
    /*
     * Example facet profile request - "facet": {}, Step 1 : For all the filter profiles requested
     * in the request(footfall/catchment), perform a two step validation. Step 1 of the validation
     * is check whether the requested filter has an associated mapping defined in the
     * filterMappingSolr.json
     */

    // For the requested facet profile see if a mapping of possible values has been defined in
    // the filterMappingSolr.json
    for (String facetVal : profileFacets) {
      if (!valueMap.containsKey(facetVal)) {
        return false;
      }
    }
    return true;
  }
}
