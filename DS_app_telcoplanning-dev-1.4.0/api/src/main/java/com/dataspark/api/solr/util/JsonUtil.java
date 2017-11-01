/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
*/
package com.dataspark.api.solr.util;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author ragarwal
 *
 * 2017-03-01
 */
public class JsonUtil {

  /**
   * One Object mapper is enough per JVM and its thread safe as per docs.
   */
  public static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
}
