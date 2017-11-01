/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.data;

import java.util.Arrays;
import java.util.List;

/**
 * @author ragarwal, 2017-03-08
 */
public class TelcoPlanningConstants {

  public static final String WEEK = "week";
  public static final Integer NUMBER_OF_DAYS_IN_WEEK = 7;
  public static final String KEY = "key";
  public static final String LABEL = "label";
  public static final String TIME_ZONE = "timeZone";
  public static final String START_DATE = "startDate";
  public static final String END_DATE = "endDate";
  public static final String PURCHASED_PERIODS = "purchasedPeriods";
  public static final String AVAILABLE_PERIODS = "availablePeriods";
  public static final String RESULT = "result";
  public static final String IS_UNLIMITED = "isUnlimited";
  public static final String GEO_HIERARCHY_CHILDREN = "children";
  public static final String UNKNOWN_ID = "unknown";
  public static final String MATCHALL_REGEX = "*";
  public static final String RANGE = "range";
  public static final String HOME_LOCATION_FILTER = "home_location";
  public static final String WORK_LOCATION_FILTER = "work_location";
  public static final String NEWLINE = "\n";
  public static final String EXPORTED_USERS_RESULTS = "results";

  public static class Indicators {
    public static final String USER_TXN_PARENT_HOME = "home";
    public static final String USER_TXN_PARENT_WORK = "work";

    public static final String USER_TXN_CHILD_CALL = "calls";
    public static final String USER_TXN_CHILD_SMS = "sms";
    public static final String USER_TXN_CHILD_UNIQUE_PEOPLE = "unique_people";
    public static final String USER_TXN_CHILD_TOTAL_STAY_DURATION = "total_stay_duration";
    public static final String USER_TXN_CHILD_AVG_STAY_DURATION = "avg_stay_duration";

    public static final String CELL_PAYLOAD = "payload";
    public static final String CELL_REVENUE = "revenue";

    public static List<String> getUserTxnParentIndicators() {
      return Arrays
          .asList(new String[] {Indicators.USER_TXN_PARENT_HOME, Indicators.USER_TXN_PARENT_WORK});
    }

    public static List<String> getUserTxnChildIndicators() {
      return Arrays.asList(new String[] {Indicators.USER_TXN_CHILD_CALL,
          Indicators.USER_TXN_CHILD_SMS, Indicators.USER_TXN_CHILD_UNIQUE_PEOPLE,
          Indicators.USER_TXN_CHILD_TOTAL_STAY_DURATION,
          Indicators.USER_TXN_CHILD_AVG_STAY_DURATION});
    }

    public static List<String> getCellIndicators() {
      return Arrays.asList(new String[] {Indicators.CELL_PAYLOAD, Indicators.CELL_REVENUE});
    }
  }

  public static class PeriodType {
    public static final String WEEK = "week";
    public static final String MONTH = "month";

    public static List<String> getAvailablePeriodTypes() {
      return Arrays.asList(new String[] {PeriodType.WEEK, PeriodType.MONTH});
    }
  }

  public class Delimiters {
    public static final String UNDERSCORE = "_";
    public static final String COLON = ":";
    public static final String COMMA = ",";
    public static final String DOT = "\\.";
    public static final String PIPE = "|";
    public static final String DASH = "-";
    public static final String SPACE = " ";
  }

  public class SolrField {
    public static final String WEEK = "week_num_ti";
    public static final String SITE_ID = "site_id_s";
    public static final String LAT_LON = "lat_lon_s";
    public static final String CGI = "cgi_s";
  }

  public class LocationType {
    public static final String HOME = "home";
    public static final String WORK = "work";
  }

  public class SolrToJsonPropMap {
    public static final String CELL_COLLECTION = "cell_collection";
    public static final String CELL_COLL_CELL_INFO = "cell_info";
    public static final String CELL_COLL_SITE_INFO = "site_info";
    public static final String ROI_STATS_COLLECTION = "roi_stats_collection";
    public static final String USER_TXN_COLLECTION = "user_txn_collection";
    public static final String USER_TXN_COLL_USER_PROFILE = "user_profile";
    public static final String SITE_INFO_HEADER = "site_info_header";

    public static final String DATA_TYPE = "dataType";
    public static final String IS_INDICATOR = "isIndicator";
    public static final String JSON_PROPERTY = "jsonProperty";
    public static final String SOLR_FIELD = "solrField";
    public static final String COLUMN_HEADER = "columnHeader";
    public static final String DO_LOOKUP = "doLookup";
    public static final String INDEX = "index";
    public static final String FORMULA = "formula";
    public static final String USER_TXN_COLL_USER_TXN = "user_txn";
    public static final String FACET_NAME = "facetName";
    public static final String AGGREGATION_TYPE = "aggregationType";
  }

  public class OutputMapField {
    public static final String SITE_ID = "siteId";
    public static final String CELL_STATS = "cellStats";
    public static final String ID = "id";
    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "lon";
    public static final String RESPONSE = "response";
    public static final String ROI_ID = "roiId";
    public static final String CELL_ID = "cellId";
  }
  
  public class AggregationType {
    public static final String AVERAGE = "average";
    public static final String MEDIAN = "median";
    public static final String SUM = "sum";
  }
}

