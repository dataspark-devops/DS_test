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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

/**
 * @author ragarwal, 2017-05-24
 */
public class RoiNodeTest {

  @Test
  public void testRoiNodeEquals() {
    Map<String, Object> roiFilters = new HashMap<String, Object>();
    roiFilters.put("roiL2", Arrays.asList(new String[] {"id1"}));
    RoiNode r1 = new RoiNode("id1", "label1", GeoDisplayType.SITES.toString(), "roiL2", "roiL1",
        roiFilters, null);
    RoiNode r2 = new RoiNode("id1", "label1", GeoDisplayType.SITES.toString(), "roiL2", "roiL1",
        roiFilters, null);

    RoiNode r3 = new RoiNode();
    r3.setId("id2");
    r3.setLabel("label2");
    r3.setDisplayType(GeoDisplayType.STATS.toString());
    r3.setAggregatableAt("roiL1");
    r3.setNodeValueIsA("roiL2");
    r3.setRoiFilters(roiFilters);
    r3.setChildren(null);

    Set<RoiNode> nodeSet = new HashSet<RoiNode>();
    nodeSet.add(r1);
    assertTrue(nodeSet.contains(r2));
    assertFalse(r2.equals(r3));
  }

  @Test
  public void testRoiNodeEqualsWithNulls() {
    RoiNode r1 =
        new RoiNode(null, "label1", GeoDisplayType.SITES.toString(), "roiL2", "roiL1", null, null);
    RoiNode r2 =
        new RoiNode(null, "label1", GeoDisplayType.SITES.toString(), "roiL2", "roiL1", null, null);
    RoiNode r3 =
        new RoiNode("id1", null, GeoDisplayType.SITES.toString(), "roiL2", "roiL1", null, null);
    RoiNode r4 =
        new RoiNode("id1", null, GeoDisplayType.SITES.toString(), "roiL2", "roiL1", null, null);
    RoiNode r5 = new RoiNode("id1", "label1", null, "roiL2", "roiL1", null, null);
    RoiNode r6 = new RoiNode("id1", "label1", null, "roiL2", "roiL1", null, null);
    RoiNode r7 =
        new RoiNode("id1", "label1", GeoDisplayType.SITES.toString(), null, "roiL1", null, null);
    RoiNode r8 =
        new RoiNode("id1", "label1", GeoDisplayType.SITES.toString(), null, "roiL1", null, null);
    RoiNode r9 =
        new RoiNode("id1", "label1", GeoDisplayType.SITES.toString(), "roiL2", null, null, null);
    RoiNode r10 =
        new RoiNode("id1", "label1", GeoDisplayType.SITES.toString(), "roiL2", null, null, null);

    assertTrue(r1.equals(r2));
    assertTrue(r3.equals(r4));
    assertTrue(r5.equals(r6));
    assertTrue(r7.equals(r8));
    assertTrue(r9.equals(r10));
  }

}
