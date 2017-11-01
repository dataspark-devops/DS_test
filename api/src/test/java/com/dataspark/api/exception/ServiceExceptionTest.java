/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
*/
package com.dataspark.api.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * @author ragarwal
 *
 * 2017-03-01
 */
public class ServiceExceptionTest {

  @Test
  public void testServiceExceptions() {

    ServiceException ex1 = new ServiceException("Test Exception Message");
    ServiceException ex2 =
        new ServiceException("Test Exception Message", new IllegalAccessException());

    try {
      throw ex1;
    } catch (Exception e) {
      assertThat(e.getClass()).isEqualTo(ServiceException.class);
    }

    try {
      throw ex2;
    } catch (Exception e) {
      assertThat(e.getClass()).isEqualTo(ServiceException.class);
    }

  }

}
