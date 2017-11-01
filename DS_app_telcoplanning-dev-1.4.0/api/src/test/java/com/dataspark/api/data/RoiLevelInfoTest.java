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
 * @author ragarwal
 *
 * 2017-03-06
 */
public class RoiLevelInfoTest {

  @Test
  public void testRoiLevelInfoMOEquals() {
    RoiLevelInfo roiLevelMO1 = new RoiLevelInfo("id1", "name1");
    RoiLevelInfo roiLevelMO2 = new RoiLevelInfo("id1", "name1");
    RoiLevelInfo roiLevelMO3 = new RoiLevelInfo();
    roiLevelMO3.setId("id2");
    roiLevelMO3.setName("name2");

    Set<RoiLevelInfo> roiLevelSet = new HashSet<RoiLevelInfo>();
    roiLevelSet.add(roiLevelMO1);
    assertTrue(roiLevelSet.contains(roiLevelMO2));
    assertFalse(roiLevelMO1.equals(roiLevelMO3));
  }

  @Test
  public void testRoiLevelInfoMOEqualsWithNulls() {
    RoiLevelInfo roiLevelMO1 = new RoiLevelInfo(null, "name1");
    RoiLevelInfo roiLevelMO2 = new RoiLevelInfo(null, "name1");

    RoiLevelInfo roiLevelMO3 = new RoiLevelInfo("id2", null);
    RoiLevelInfo roiLevelMO4 = new RoiLevelInfo("id2", null);
    
    RoiLevelInfo roiLevelMO5 = new RoiLevelInfo();
    RoiLevelInfo roiLevelMO6 = new RoiLevelInfo();


    assertTrue(roiLevelMO1.equals(roiLevelMO2));
    assertTrue(roiLevelMO3.equals(roiLevelMO4));
    assertTrue(roiLevelMO5.equals(roiLevelMO6));
    assertTrue(roiLevelMO1.equals(roiLevelMO1));
    assertFalse(roiLevelMO1.equals(roiLevelMO5));
    assertFalse(roiLevelMO5.equals(roiLevelMO1));
    
    assertTrue(roiLevelMO2.hashCode() == roiLevelMO1.hashCode());
    assertTrue(roiLevelMO5.hashCode() == roiLevelMO6.hashCode());


  }
}
