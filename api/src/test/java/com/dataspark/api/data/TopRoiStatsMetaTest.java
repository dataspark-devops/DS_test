/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

/**
 * @author ragarwal, 2017-03-17
 */
public class TopRoiStatsMetaTest {

  @Test
  public void testTopRoiStatsMetaMOEquals() {
    List<Map<String, String>> indicators = new ArrayList<Map<String, String>>();
    Map<String, String> indMap = new HashMap<String, String>();
    indMap.put("indicator.key1", "indicator.value1");
    indicators.add(indMap);
    TopRoiStatsMeta t1 = new TopRoiStatsMeta("01", "2017", indicators);
    TopRoiStatsMeta t2 = new TopRoiStatsMeta("01", "2017", indicators);

    TopRoiStatsMeta t3 = new TopRoiStatsMeta();
    t3.setMonth("02");
    t3.setYear("2017");
    t3.setIndicators(indicators);

    Set<TopRoiStatsMeta> metaSet = new HashSet<TopRoiStatsMeta>();
    metaSet.add(t1);

    assertTrue(metaSet.contains(t2));
    assertFalse(t2.equals(t3));
  }

  @Test
  public void testTopRoiStatsMetaMOEqualsWithNulls() {
    List<Map<String, String>> indicators = new ArrayList<Map<String, String>>();
    Map<String, String> indMap = new HashMap<String, String>();
    indMap.put("indicator.key1", "indicator.value1");
    indicators.add(indMap);
    TopRoiStatsMeta t1 = new TopRoiStatsMeta(null, "2017", indicators);
    TopRoiStatsMeta t2 = new TopRoiStatsMeta(null, "2017", indicators);

    TopRoiStatsMeta t3 = new TopRoiStatsMeta("01", null, indicators);
    TopRoiStatsMeta t4 = new TopRoiStatsMeta("01", null, indicators);

    TopRoiStatsMeta t5 = new TopRoiStatsMeta("01", "2017", null);
    TopRoiStatsMeta t6 = new TopRoiStatsMeta("01", "2017", null);

    assertTrue(t1.equals(t2));
    assertTrue(t3.equals(t4));
    assertTrue(t5.equals(t6));

  }

}
