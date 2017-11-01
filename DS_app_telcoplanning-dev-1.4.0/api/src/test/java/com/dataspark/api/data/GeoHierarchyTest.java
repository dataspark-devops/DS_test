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

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

/**
 * @author ragarwal, 2017-05-24
 */
public class GeoHierarchyTest {

  @Test
  public void testGeoHierarchyEquals() {
    GeoHierarchy g1 = new GeoHierarchy(GeoNodeType.ROI_LEVEL, GeoNodeDepth.roiL1,
        GeoDisplayType.STATS, GeoNodeDepth.roiL1, null);
    GeoHierarchy g2 = new GeoHierarchy(GeoNodeType.ROI_LEVEL, GeoNodeDepth.roiL1,
        GeoDisplayType.STATS, GeoNodeDepth.roiL1, null);
    GeoHierarchy g3 = new GeoHierarchy(GeoNodeType.ROI_LEVEL, GeoNodeDepth.roiL1,
        GeoDisplayType.STATS, GeoNodeDepth.roiL2, null);

    Set<GeoHierarchy> geoSet = new HashSet<GeoHierarchy>();
    geoSet.add(g1);

    assertTrue(geoSet.contains(g2));
    assertFalse(g2.equals(g3));
  }

  @Test
  public void testGeoHierarchyWithNulls() {
    GeoHierarchy g1 =
        new GeoHierarchy(null, GeoNodeDepth.roiL1, GeoDisplayType.STATS, GeoNodeDepth.roiL1, null);
    GeoHierarchy g2 =
        new GeoHierarchy(null, GeoNodeDepth.roiL1, GeoDisplayType.STATS, GeoNodeDepth.roiL1, null);

    GeoHierarchy g3 = new GeoHierarchy(GeoNodeType.ROI_LEVEL, null, GeoDisplayType.STATS,
        GeoNodeDepth.roiL1, null);
    GeoHierarchy g4 = new GeoHierarchy(GeoNodeType.ROI_LEVEL, null, GeoDisplayType.STATS,
        GeoNodeDepth.roiL1, null);

    GeoHierarchy g5 =
        new GeoHierarchy(GeoNodeType.ROI_LEVEL, GeoNodeDepth.roiL1, null, GeoNodeDepth.roiL1, null);
    GeoHierarchy g6 =
        new GeoHierarchy(GeoNodeType.ROI_LEVEL, GeoNodeDepth.roiL1, null, GeoNodeDepth.roiL1, null);

    GeoHierarchy g7 = new GeoHierarchy(GeoNodeType.ROI_LEVEL, GeoNodeDepth.roiL1,
        GeoDisplayType.STATS, null, null);
    GeoHierarchy g8 = new GeoHierarchy(GeoNodeType.ROI_LEVEL, GeoNodeDepth.roiL1,
        GeoDisplayType.STATS, null, null);

    assertTrue(g1.equals(g2));
    assertTrue(g3.equals(g4));
    assertTrue(g5.equals(g6));
    assertTrue(g7.equals(g8));
  }

}
