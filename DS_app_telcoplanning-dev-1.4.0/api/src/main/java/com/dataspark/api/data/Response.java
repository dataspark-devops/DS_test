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

/**
 * @author ragarwal, 2017-03-15
 */
public class Response implements Serializable {

  private static final long serialVersionUID = 1746245746026923403L;
  private Meta meta;
  private List<Object[]> results;

  public Response() {}

  public Response(Meta meta, List<Object[]> results) {
    super();
    this.meta = meta;
    this.results = results;
  }

  public Meta getMeta() {
    return meta;
  }

  public void setMeta(Meta meta) {
    this.meta = meta;
  }

  public List<Object[]> getResults() {
    return results;
  }

  public void setResults(List<Object[]> results) {
    this.results = results;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((meta == null) ? 0 : meta.hashCode());
    result = prime * result + ((results == null) ? 0 : results.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Response other = (Response) obj;
    if (meta == null) {
      if (other.meta != null)
        return false;
    } else if (!meta.equals(other.meta))
      return false;
    if (results == null) {
      if (other.results != null)
        return false;
    } else if (!results.equals(other.results))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "ResponseMO [meta=" + meta + ", results=" + results + "]";
  }

}
