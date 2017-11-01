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
public class TopRoiStatsResponseTest {

  @Test
  public void testTopRoiStatsResponseMOEquals() {
    List<Map<String, String>> indicators = new ArrayList<Map<String, String>>();
    Map<String, String> indMap = new HashMap<String, String>();
    indMap.put("indicator.key1", "indicator.value1");
    indicators.add(indMap);
    TopRoiStatsMeta meta = new TopRoiStatsMeta("01", "2017", indicators);

    List<Map<String, Object>> roiStats = new ArrayList<Map<String, Object>>();
    roiStats.add(createTopRoiStats("roiname", "roiid", 12d, 2d, 1d, 1d));

    TopRoiStatsResponse t1 = new TopRoiStatsResponse(meta, roiStats);
    TopRoiStatsResponse t2 = new TopRoiStatsResponse(meta, roiStats);
    TopRoiStatsResponse t3 = new TopRoiStatsResponse();
    t3.setMeta(meta);
    roiStats = new ArrayList<Map<String, Object>>();
    roiStats.add(createTopRoiStats("roiname", "roiid", 12d, 2d, 1d, 10d));
    t3.setTopRoiStats(roiStats);

    Set<TopRoiStatsResponse> respSet = new HashSet<TopRoiStatsResponse>();
    respSet.add(t1);

    assertTrue(respSet.contains(t2));
    assertFalse(t2.equals(t3));
  }

  @Test
  public void testTopRoiStatsResponseMOEqualsWithNulls() {
    List<Map<String, String>> indicators = new ArrayList<Map<String, String>>();
    Map<String, String> indMap = new HashMap<String, String>();
    indMap.put("indicator.key1", "indicator.value1");
    indicators.add(indMap);
    TopRoiStatsMeta meta = new TopRoiStatsMeta("01", "2017", indicators);

    List<Map<String, Object>> roiStats = new ArrayList<Map<String, Object>>();
    roiStats.add(createTopRoiStats("roiname", "roiid", 12d, 2d, 1d, 1d));

    TopRoiStatsResponse t1 = new TopRoiStatsResponse(null, roiStats);
    TopRoiStatsResponse t2 = new TopRoiStatsResponse(null, roiStats);

    TopRoiStatsResponse t3 = new TopRoiStatsResponse(meta, null);
    TopRoiStatsResponse t4 = new TopRoiStatsResponse(meta, null);

    assertTrue(t1.equals(t2));
    assertTrue(t3.equals(t4));
  }

  private Map<String, Object> createTopRoiStats(String roiName, String roiId, double revenue,
      double payload, double revenueThres, double payloadThreshold) {
    Map<String, Object> roiStats = new HashMap<String, Object>();
    roiStats.put("roi1_id_s", roiId);
    roiStats.put("roi1_name_s", roiName);
    roiStats.put("rev_td", revenue);
    roiStats.put("rev_thre_td", revenueThres);
    roiStats.put("vol_td", payload);
    roiStats.put("vol_thre_td", payloadThreshold);
    return roiStats;
  }
}
