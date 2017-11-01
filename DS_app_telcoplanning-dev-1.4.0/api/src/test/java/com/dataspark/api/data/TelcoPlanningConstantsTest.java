/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.data;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author ragarwal, 2017-03-17
 */
public class TelcoPlanningConstantsTest {

  @Test
  public void testUserTxnParentIndicators() {
    assertTrue(TelcoPlanningConstants.Indicators.getUserTxnParentIndicators()
        .contains(TelcoPlanningConstants.Indicators.USER_TXN_PARENT_HOME));

    assertTrue(TelcoPlanningConstants.Indicators.getUserTxnParentIndicators()
        .contains(TelcoPlanningConstants.Indicators.USER_TXN_PARENT_WORK));

    assertTrue(TelcoPlanningConstants.Indicators.getUserTxnParentIndicators().size() == 2);
  }

  @Test
  public void testUserTxnChildIndicators() {
    assertTrue(TelcoPlanningConstants.Indicators.getUserTxnChildIndicators()
        .contains(TelcoPlanningConstants.Indicators.USER_TXN_CHILD_CALL));

    assertTrue(TelcoPlanningConstants.Indicators.getUserTxnChildIndicators()
        .contains(TelcoPlanningConstants.Indicators.USER_TXN_CHILD_SMS));

    assertTrue(TelcoPlanningConstants.Indicators.getUserTxnChildIndicators()
        .contains(TelcoPlanningConstants.Indicators.USER_TXN_CHILD_UNIQUE_PEOPLE));
    
    assertTrue(TelcoPlanningConstants.Indicators.getUserTxnChildIndicators()
        .contains(TelcoPlanningConstants.Indicators.USER_TXN_CHILD_TOTAL_STAY_DURATION));
    
    assertTrue(TelcoPlanningConstants.Indicators.getUserTxnChildIndicators()
        .contains(TelcoPlanningConstants.Indicators.USER_TXN_CHILD_AVG_STAY_DURATION));

    assertTrue(TelcoPlanningConstants.Indicators.getUserTxnChildIndicators().size() == 5);
  }

  @Test
  public void testCellIndicators() {
    assertTrue(TelcoPlanningConstants.Indicators.getCellIndicators()
        .contains(TelcoPlanningConstants.Indicators.CELL_PAYLOAD));

    assertTrue(TelcoPlanningConstants.Indicators.getCellIndicators()
        .contains(TelcoPlanningConstants.Indicators.CELL_REVENUE));

    assertTrue(TelcoPlanningConstants.Indicators.getCellIndicators().size() == 2);
  }

  @Test
  public void testPeriodType() {
    assertTrue(TelcoPlanningConstants.PeriodType.getAvailablePeriodTypes()
        .contains(TelcoPlanningConstants.PeriodType.WEEK));

    assertTrue(TelcoPlanningConstants.PeriodType.getAvailablePeriodTypes()
        .contains(TelcoPlanningConstants.PeriodType.MONTH));

    assertTrue(TelcoPlanningConstants.PeriodType.getAvailablePeriodTypes().size() == 2);
  }
}
