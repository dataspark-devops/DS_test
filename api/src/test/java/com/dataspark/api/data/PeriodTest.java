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
 * @author ragarwal, 2017-03-15
 */
public class PeriodTest {

  @Test
  public void testPeriodMOEquals() {
    String startDate = "2017-01-01T00:00:00+07:00";
    String endDate1 = "2017-01-08T00:00:00+07:00";
    String endDate2 = "2017-01-016T00:00:00+07:00";

    Period p1 = new Period(startDate, endDate1);
    Period p2 = new Period(startDate, endDate1);

    Period p3 = new Period();
    p3.setStartDate(startDate);
    p3.setEndDate(endDate2);

    Set<Period> pSet = new HashSet<Period>();
    pSet.add(p1);
    assertTrue(pSet.contains(p2));
    assertFalse(p2.equals(p3));
  }

  @Test
  public void testPeriodMOEqualsWithNulls() {
    String date = "2017-01-01T00:00:00+07:00";
    Period p1 = new Period(date, null);
    Period p2 = new Period(date, null);

    Period p3 = new Period(null, date);
    Period p4 = new Period(null, date);

    assertTrue(p1.equals(p2));
    assertTrue(p3.equals(p4));
  }
}
