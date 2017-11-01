/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.data;

import java.util.Arrays;

/**
 * @author ragarwal, 2017-03-15
 */
public class Meta extends CellDetailsRequest {

  private static final long serialVersionUID = -6587374647147727761L;
  private String order[];

  public Meta() {}

  public Meta(String[] order) {
    super();
    this.order = order;
  }

  public String[] getOrder() {
    return order;
  }

  public void setOrder(String[] order) {
    this.order = order;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(order);
    return result;
  }
}
