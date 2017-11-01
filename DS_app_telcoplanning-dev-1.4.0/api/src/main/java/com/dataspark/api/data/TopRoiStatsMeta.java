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
 * @author ragarwal, 2017-03-13
 */
public class TopRoiStatsMeta implements Serializable {

  private static final long serialVersionUID = -3936837083347304946L;
  private String month;
  private String year;
  private List<Map<String, String>> indicators;

  public TopRoiStatsMeta() {}

  public TopRoiStatsMeta(String month, String year, List<Map<String, String>> indicators) {
    this.month = month;
    this.year = year;
    this.indicators = indicators;
  }

  public String getMonth() {
    return month;
  }

  public void setMonth(String month) {
    this.month = month;
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
  }

  public List<Map<String, String>> getIndicators() {
    return indicators;
  }

  public void setIndicators(List<Map<String, String>> indicators) {
    this.indicators = indicators;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 17;
    result = prime * result + ((indicators == null) ? 0 : indicators.hashCode());
    result = prime * result + ((month == null) ? 0 : month.hashCode());
    result = prime * result + ((year == null) ? 0 : year.hashCode());
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
    TopRoiStatsMeta other = (TopRoiStatsMeta) obj;
    if (indicators == null) {
      if (other.indicators != null)
        return false;
    } else if (!indicators.equals(other.indicators))
      return false;
    if (month == null) {
      if (other.month != null)
        return false;
    } else if (!month.equals(other.month))
      return false;
    if (year == null) {
      if (other.year != null)
        return false;
    } else if (!year.equals(other.year))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "TopRoiStatsMetaMO [month=" + month + ", year=" + year + ", indicators=" + indicators
        + "]";
  }

}
