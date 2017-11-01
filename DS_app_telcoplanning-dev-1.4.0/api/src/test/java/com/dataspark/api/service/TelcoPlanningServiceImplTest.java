/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.service;

import static org.junit.Assert.assertTrue;

import com.dataspark.api.data.TelcoPlanningConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author ragarwal, 2017-03-29
 */
public class TelcoPlanningServiceImplTest {

  @BeforeClass
  public static void setup() {
    System.setProperty("user.timezone", "Asia/Jakarta");
  }

  // Unlimited Purchased Periods AND Continuous Available Collections

  @Test
  public void testAvailablePeriodsForUnknownUser() {
    // Purchased periods = null
    List<Map<String, String>> availablePeriods = new TelcoPlanningServiceHelper().getAvailablePeriods(
        DateTimeZone.forID("Asia/Jakarta"), null, Arrays.asList(new Integer[] {201701}));
    assertTrue(availablePeriods.size() == 0);
  }

  @Test
  public void testAvailablePeriodsForUnlimitedUserForOneAvailableCollection() {
    List<Map<String, Object>> ppList = getPurchasedPeriodsForUnlimitedUser();
    List<Map<String, String>> availablePeriods = new TelcoPlanningServiceHelper().getAvailablePeriods(
        DateTimeZone.forID("Asia/Jakarta"), ppList, Arrays.asList(new Integer[] {201701}));
    assertTrue(availablePeriods.size() == 1);
    assertTrue(new DateTime("2017-01-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.START_DATE))) == 0);
    assertTrue(new DateTime("2017-02-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.END_DATE))) == 0);
  }

  @Test
  public void testAvailablePeriodsForUnlimitedUserForMultipleAvailableCollections() {
    List<Map<String, Object>> ppList = getPurchasedPeriodsForUnlimitedUser();
    List<Map<String, String>> availablePeriods = new TelcoPlanningServiceHelper().getAvailablePeriods(
        DateTimeZone.forID("Asia/Jakarta"), ppList, Arrays.asList(new Integer[] {201701, 201702}));
    assertTrue(availablePeriods.size() == 2);
    assertTrue(new DateTime("2017-01-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.START_DATE))) == 0);
    assertTrue(new DateTime("2017-02-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.END_DATE))) == 0);
    assertTrue(new DateTime("2017-02-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(1).get(TelcoPlanningConstants.START_DATE))) == 0);
    assertTrue(new DateTime("2017-03-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(1).get(TelcoPlanningConstants.END_DATE))) == 0);
  }

  // Continuous Purchased Periods AND Continuous Available Collections

  @Test
  public void testAvailablePeriodsWhenPPSameAsAvailableCollections() {
    // Purchased Periods = Jan 1, 2017 to Jan 31, 2017
    // Available Collections = Jan 1, 2017 to Jan 31, 2017
    List<Map<String, Object>> ppList = getPurchasedPeriodsForGivenDates(
        "2017-01-01T00:00:00.000+07:00", "2017-02-01T00:00:00.000+07:00");
    List<Map<String, String>> availablePeriods = new TelcoPlanningServiceHelper().getAvailablePeriods(
        DateTimeZone.forID("Asia/Jakarta"), ppList, Arrays.asList(new Integer[] {201701}));
    assertTrue(availablePeriods.size() == 1);
    assertTrue(new DateTime("2017-01-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.START_DATE))) == 0);
    assertTrue(new DateTime("2017-02-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.END_DATE))) == 0);
  }

  @Test
  public void testAvailablePeriodsWhenPPEndsAfterAvailableCollections() {
    // Purchased Periods = Jan 1, 2017 to Mar 9, 2017
    // Available Collections = Jan 1, 2017 to Feb 28, 2017
    List<Map<String, Object>> ppList = getPurchasedPeriodsForGivenDates(
        "2017-01-01T00:00:00.000+07:00", "2017-03-10T00:00:00.000+07:00");
    List<Map<String, String>> availablePeriods = new TelcoPlanningServiceHelper().getAvailablePeriods(
        DateTimeZone.forID("Asia/Jakarta"), ppList, Arrays.asList(new Integer[] {201701, 201702}));
    assertTrue(availablePeriods.size() == 2);
    assertTrue(new DateTime("2017-01-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.START_DATE))) == 0);
    assertTrue(new DateTime("2017-02-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.END_DATE))) == 0);
    assertTrue(new DateTime("2017-02-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(1).get(TelcoPlanningConstants.START_DATE))) == 0);
    assertTrue(new DateTime("2017-03-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(1).get(TelcoPlanningConstants.END_DATE))) == 0);
  }

  @Test
  public void testAvailablePeriodsWhenPPStartsBeforeAvailableCollections() {
    // Purchased Periods = Dec 1, 2016 to Feb 28, 2017
    // Available Collections = Jan 1, 2017 to Feb 28, 2017
    List<Map<String, Object>> ppList = getPurchasedPeriodsForGivenDates(
        "2016-12-01T00:00:00.000+07:00", "2017-03-01T00:00:00.000+07:00");
    List<Map<String, String>> availablePeriods = new TelcoPlanningServiceHelper().getAvailablePeriods(
        DateTimeZone.forID("Asia/Jakarta"), ppList, Arrays.asList(new Integer[] {201701, 201702}));
    assertTrue(availablePeriods.size() == 2);
    assertTrue(new DateTime("2017-01-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.START_DATE))) == 0);
    assertTrue(new DateTime("2017-02-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.END_DATE))) == 0);
    assertTrue(new DateTime("2017-02-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(1).get(TelcoPlanningConstants.START_DATE))) == 0);
    assertTrue(new DateTime("2017-03-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(1).get(TelcoPlanningConstants.END_DATE))) == 0);
  }

  @Test
  public void testAvailablePeriodsWhenPPStartsBeforeAndEndsAfterAvailableCollections() {
    // Purchased Periods = Dec 1, 2016 to Mar 9, 2017
    // Available Collections = Jan 1, 2017 to Feb 28, 2017
    List<Map<String, Object>> ppList = getPurchasedPeriodsForGivenDates(
        "2016-12-01T00:00:00.000+07:00", "2017-03-10T00:00:00.000+07:00");
    List<Map<String, String>> availablePeriods = new TelcoPlanningServiceHelper().getAvailablePeriods(
        DateTimeZone.forID("Asia/Jakarta"), ppList, Arrays.asList(new Integer[] {201701, 201702}));
    assertTrue(availablePeriods.size() == 2);
    assertTrue(new DateTime("2017-01-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.START_DATE))) == 0);
    assertTrue(new DateTime("2017-02-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.END_DATE))) == 0);
    assertTrue(new DateTime("2017-02-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(1).get(TelcoPlanningConstants.START_DATE))) == 0);
    assertTrue(new DateTime("2017-03-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(1).get(TelcoPlanningConstants.END_DATE))) == 0);
  }

  @Test
  public void testAvailablePeriodsWhenPPStartsAndEndsAfterAvailableCollections() {
    // Purchased Periods = Jan 10, 2017 to Mar 9, 2017
    // Available Collections = Jan 1, 2017 to Feb 28, 2017
    List<Map<String, Object>> ppList = getPurchasedPeriodsForGivenDates(
        "2017-01-10T00:00:00.000+07:00", "2017-03-10T00:00:00.000+07:00");
    List<Map<String, String>> availablePeriods = new TelcoPlanningServiceHelper().getAvailablePeriods(
        DateTimeZone.forID("Asia/Jakarta"), ppList, Arrays.asList(new Integer[] {201701, 201702}));
    assertTrue(availablePeriods.size() == 2);

    System.out.println(
        " testAvailablePeriodsWhenPPStartsAndEndsAfterAvailableCollections availablePeriods: "
            + availablePeriods);
    assertTrue(new DateTime("2017-01-10T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.START_DATE))) == 0);
    assertTrue(new DateTime("2017-02-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.END_DATE))) == 0);
    assertTrue(new DateTime("2017-02-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(1).get(TelcoPlanningConstants.START_DATE))) == 0);
    assertTrue(new DateTime("2017-03-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(1).get(TelcoPlanningConstants.END_DATE))) == 0);
  }

  @Test
  public void testAvailablePeriodsWhenPPStartsAndEndsBeforeAvailableCollections() {
    // Purchased Periods = Dec 1, 2016 to Feb 9, 2017
    // Available Collections = Jan 1, 2017 to Feb 28, 2017
    List<Map<String, Object>> ppList = getPurchasedPeriodsForGivenDates(
        "2016-12-01T00:00:00.000+07:00", "2017-02-10T00:00:00.000+07:00");
    List<Map<String, String>> availablePeriods = new TelcoPlanningServiceHelper().getAvailablePeriods(
        DateTimeZone.forID("Asia/Jakarta"), ppList, Arrays.asList(new Integer[] {201701, 201702}));
    assertTrue(availablePeriods.size() == 2);
    System.out.println(
        " testAvailablePeriodsWhenPPStartsAndEndsBeforeAvailableCollections availablePeriods: "
            + availablePeriods);
    assertTrue(new DateTime("2017-01-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.START_DATE))) == 0);
    assertTrue(new DateTime("2017-02-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.END_DATE))) == 0);
    assertTrue(new DateTime("2017-02-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(1).get(TelcoPlanningConstants.START_DATE))) == 0);
    assertTrue(new DateTime("2017-02-10T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(1).get(TelcoPlanningConstants.END_DATE))) == 0);
  }

  @Test
  public void testAvailablePeriodsWhenPPStartsAndEndsBeforeAvailableCollectionsNoOverlap() {
    // Purchased Periods = Dec 1, 2016 to Dec 31, 2016
    // Available Collections = Jan 1, 2017 to Feb 28, 2017
    List<Map<String, Object>> ppList = getPurchasedPeriodsForGivenDates(
        "2016-12-01T00:00:00.000+07:00", "2017-01-01T00:00:00.000+07:00");
    List<Map<String, String>> availablePeriods = new TelcoPlanningServiceHelper().getAvailablePeriods(
        DateTimeZone.forID("Asia/Jakarta"), ppList, Arrays.asList(new Integer[] {201701, 201702}));
    assertTrue(availablePeriods.size() == 0);
  }

  @Test
  public void testAvailablePeriodsWhenPPStartsAndEndsAfterAvailableCollectionsNoOverlap() {
    // Purchased Periods = Mar 1, 2017 to Mar 31, 2017
    // Available Collections = Jan 1, 2017 to Feb 28, 2017
    List<Map<String, Object>> ppList = getPurchasedPeriodsForGivenDates(
        "2017-03-01T00:00:00.000+07:00", "2017-04-01T00:00:00.000+07:00");
    List<Map<String, String>> availablePeriods = new TelcoPlanningServiceHelper().getAvailablePeriods(
        DateTimeZone.forID("Asia/Jakarta"), ppList, Arrays.asList(new Integer[] {201701, 201702}));
    assertTrue(availablePeriods.size() == 0);
  }

  // Continuous Purchased Periods AND Discontinuous Available Collections

  @Test
  public void testAvailablePeriodsWhenPPEndsAfterDiscontinuousAvailableCollections() {
    // Purchased Periods = Jan 1, 2017 to April 9, 2017
    // Available Collections = Jan 1, 2017 to Jan 31, 2017 AND Mar 1, 2017 to Mar 31, 2017
    List<Map<String, Object>> ppList = getPurchasedPeriodsForGivenDates(
        "2017-01-01T00:00:00.000+07:00", "2017-04-10T00:00:00.000+07:00");
    List<Map<String, String>> availablePeriods = new TelcoPlanningServiceHelper().getAvailablePeriods(
        DateTimeZone.forID("Asia/Jakarta"), ppList, Arrays.asList(new Integer[] {201701, 201703}));
    assertTrue(availablePeriods.size() == 2);
    assertTrue(new DateTime("2017-01-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.START_DATE))) == 0);
    assertTrue(new DateTime("2017-02-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.END_DATE))) == 0);
    assertTrue(new DateTime("2017-03-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(1).get(TelcoPlanningConstants.START_DATE))) == 0);
    assertTrue(new DateTime("2017-04-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(1).get(TelcoPlanningConstants.END_DATE))) == 0);
  }

  @Test
  public void testAvailablePeriodsWhenPPStartsBeforeDiscontinuousAvailableCollections() {
    // Purchased Periods = Dec 1, 2016 to Mar 31, 2017
    // Available Collections = Jan 1, 2017 to Jan 31, 2017 AND Mar 1, 2017 to Mar 31, 2017
    List<Map<String, Object>> ppList = getPurchasedPeriodsForGivenDates(
        "2016-12-01T00:00:00.000+07:00", "2017-04-01T00:00:00.000+07:00");
    List<Map<String, String>> availablePeriods = new TelcoPlanningServiceHelper().getAvailablePeriods(
        DateTimeZone.forID("Asia/Jakarta"), ppList, Arrays.asList(new Integer[] {201701, 201702}));
    assertTrue(availablePeriods.size() == 2);
    assertTrue(new DateTime("2017-01-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.START_DATE))) == 0);
    assertTrue(new DateTime("2017-02-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.END_DATE))) == 0);
    assertTrue(new DateTime("2017-02-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(1).get(TelcoPlanningConstants.START_DATE))) == 0);
    assertTrue(new DateTime("2017-03-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(1).get(TelcoPlanningConstants.END_DATE))) == 0);
  }

  @Test
  public void testAvailablePeriodsWhenPPStartsBeforeAndEndsAfterDiscontinuousAvailableCollections() {
    // Purchased Periods = Dec 1, 2016 to Mar 31, 2017
    // Available Collections = Jan 1, 2017 to Jan 31, 2017 AND Mar 1, 2017 to Mar 31, 2017
    List<Map<String, Object>> ppList = getPurchasedPeriodsForGivenDates(
        "2012-12-01T00:00:00.000+07:00", "2017-04-01T00:00:00.000+07:00");
    List<Map<String, String>> availablePeriods = new TelcoPlanningServiceHelper().getAvailablePeriods(
        DateTimeZone.forID("Asia/Jakarta"), ppList, Arrays.asList(new Integer[] {201701, 201703}));
    assertTrue(availablePeriods.size() == 2);
    assertTrue(new DateTime("2017-01-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.START_DATE))) == 0);
    assertTrue(new DateTime("2017-02-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.END_DATE))) == 0);
    assertTrue(new DateTime("2017-03-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(1).get(TelcoPlanningConstants.START_DATE))) == 0);
    assertTrue(new DateTime("2017-04-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(1).get(TelcoPlanningConstants.END_DATE))) == 0);
  }

  @Test
  public void testAvailablePeriodsWhenPPStartsAndEndsAfterDiscontinuousAvailableCollections() {
    // Purchased Periods = Jan 10, 2017 to Apr 9, 2017
    // Available Collections = Jan 1, 2017 to Jan 31, 2017 AND Mar 1, 2017 to Mar 31, 2017
    List<Map<String, Object>> ppList = getPurchasedPeriodsForGivenDates(
        "2017-01-10T00:00:00.000+07:00", "2017-04-10T00:00:00.000+07:00");
    List<Map<String, String>> availablePeriods = new TelcoPlanningServiceHelper().getAvailablePeriods(
        DateTimeZone.forID("Asia/Jakarta"), ppList, Arrays.asList(new Integer[] {201701, 201703}));
    assertTrue(availablePeriods.size() == 2);
    assertTrue(new DateTime("2017-01-10T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.START_DATE))) == 0);
    assertTrue(new DateTime("2017-02-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.END_DATE))) == 0);
    assertTrue(new DateTime("2017-03-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(1).get(TelcoPlanningConstants.START_DATE))) == 0);
    assertTrue(new DateTime("2017-04-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(1).get(TelcoPlanningConstants.END_DATE))) == 0);
  }

  @Test
  public void testAvailablePeriodsWhenPPStartsAndEndsBeforeDiscontinuousAvailableCollections() {
    // Purchased Periods = Dec 1, 2016 to Feb 9, 2017
    // Available Collections = Jan 1, 2017 to Jan 31, 2017 AND Mar 1, 2017 to Mar 31, 2017
    List<Map<String, Object>> ppList = getPurchasedPeriodsForGivenDates(
        "2016-12-01T00:00:00.000+07:00", "2017-02-10T00:00:00.000+07:00");
    List<Map<String, String>> availablePeriods = new TelcoPlanningServiceHelper().getAvailablePeriods(
        DateTimeZone.forID("Asia/Jakarta"), ppList, Arrays.asList(new Integer[] {201701, 201703}));
    assertTrue(availablePeriods.size() == 1);
    assertTrue(new DateTime("2017-01-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.START_DATE))) == 0);
    assertTrue(new DateTime("2017-02-01T00:00:00.000+07:00").compareTo(
        new DateTime(availablePeriods.get(0).get(TelcoPlanningConstants.END_DATE))) == 0);
  }

  @Test
  public void testAvailablePeriodsWhenPPStartsAndEndsBeforeDiscontinuousAvailableCollectionsNoOverlap() {
    // Purchased Periods = Dec 1, 2016 to Dec 31, 2016
    // Available Collections = Jan 1, 2017 to Jan 31, 2017 AND Mar 1, 2017 to Mar 31, 2017
    List<Map<String, Object>> ppList = getPurchasedPeriodsForGivenDates(
        "2016-12-01T00:00:00.000+07:00", "2017-01-01T00:00:00.000+07:00");
    List<Map<String, String>> availablePeriods = new TelcoPlanningServiceHelper().getAvailablePeriods(
        DateTimeZone.forID("Asia/Jakarta"), ppList, Arrays.asList(new Integer[] {201701, 201702}));
    assertTrue(availablePeriods.size() == 0);
  }

  @Test
  public void testAvailablePeriodsWhenPPStartsAndEndsAfterDiscontinuousAvailableCollectionsNoOverlap() {
    // Purchased Periods = Apr 1, 2017 to Apr 31, 2017
    // Available Collections = Jan 1, 2017 to Jan 31, 2017 AND Mar 1, 2017 to Mar 31, 2017
    List<Map<String, Object>> ppList = getPurchasedPeriodsForGivenDates(
        "2017-04-01T00:00:00.000+07:00", "2017-05-01T00:00:00.000+07:00");
    List<Map<String, String>> availablePeriods =
        new TelcoPlanningServiceHelper().getAvailablePeriods(
        DateTimeZone.forID("Asia/Jakarta"), ppList, Arrays.asList(new Integer[] {201701, 201703}));
    assertTrue(availablePeriods.size() == 0);
  }

  private List<Map<String, Object>> getPurchasedPeriodsForGivenDates(String startDt, String endDt) {
    List<Map<String, Object>> ppList = new ArrayList<Map<String, Object>>();
    Map<String, Object> ppMap = new HashMap<String, Object>();
    ppMap.put(TelcoPlanningConstants.IS_UNLIMITED, false);
    ppMap.put(TelcoPlanningConstants.START_DATE, startDt);
    ppMap.put(TelcoPlanningConstants.END_DATE, endDt);
    ppList.add(ppMap);
    return ppList;
  }

  private List<Map<String, Object>> getPurchasedPeriodsForUnlimitedUser() {
    List<Map<String, Object>> ppList = new ArrayList<Map<String, Object>>();
    Map<String, Object> ppMap = new HashMap<String, Object>();
    ppMap.put(TelcoPlanningConstants.IS_UNLIMITED, true);
    ppList.add(ppMap);
    return ppList;
  }

}
