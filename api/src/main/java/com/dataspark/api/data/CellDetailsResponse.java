/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author ragarwal, 2017-03-14
 */
public class CellDetailsResponse implements Serializable {

  private static final long serialVersionUID = -6110818340009137436L;
  private CellDetailsRequest meta;
  private List<Map<String, Object>> results;

  public CellDetailsRequest getMeta() {
    return meta;
  }

  public void setMeta(CellDetailsRequest meta) {
    this.meta = meta;
  }

  public List<Map<String, Object>> getResults() {
    return results;
  }

  public void setResults(List<Map<String, Object>> results) {
    this.results = results;
  }

  @Override
  public String toString() {
    return "CellDetailsResponse [meta=" + meta + ", results=" + results + "]";
  }

}
