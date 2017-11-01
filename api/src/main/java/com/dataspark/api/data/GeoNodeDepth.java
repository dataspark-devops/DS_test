/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.data;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author ragarwal, 2017-05-03
 */
public enum GeoNodeDepth {

  roiL0("nation"), roiL1("bbc"), roiL2("kabupaten"), roiL3("kecamatan"), roiL4("kelurahan"), roiL5(
      "all_sites");

  private final String value;
  public static List<GeoNodeDepth> absentinGeoJson =
      Arrays.asList(new GeoNodeDepth[] {GeoNodeDepth.roiL0, GeoNodeDepth.roiL5});

  private GeoNodeDepth(String value) {
    this.value = value;
  }

  public String getRoiName() {
    return value;
  }

  public static LinkedHashMap<String, String> getRoiLevelsMap() {
    LinkedHashMap<String, String> roiLevelsMap = new LinkedHashMap<String, String>();

    for (GeoNodeDepth geoDepth : GeoNodeDepth.values()) {
      if (!absentinGeoJson.contains(geoDepth)) {
        roiLevelsMap.put(geoDepth.name(), geoDepth.getRoiName());
      }
    }
    return roiLevelsMap;
  }
}
