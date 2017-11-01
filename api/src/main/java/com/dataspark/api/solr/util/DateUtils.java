package com.dataspark.api.solr.util;

import com.dataspark.api.data.Period;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.joda.time.DateTime;

public class DateUtils {
  public static int getWeekNumber(DateTime dt) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, dt.getYear());
    cal.set(Calendar.MONTH, (dt.getMonthOfYear() - 1));
    cal.set(Calendar.DAY_OF_MONTH, dt.getDayOfMonth());
    cal.setFirstDayOfWeek(Calendar.MONDAY);
    cal.setMinimalDaysInFirstWeek(7);
    int week = cal.get(Calendar.WEEK_OF_YEAR);
    return week;
  }

  public static Set<Integer> getWeekNumbers(List<Period> periods) throws ParseException {
    Set<Integer> weeks = new HashSet<Integer>();
    for (Period period : periods) {
      DateTime d1 = new DateTime(period.getStartDate().substring(0, 10));
      Calendar cal = Calendar.getInstance();
      cal.setTime(d1.toDate());
      cal.setFirstDayOfWeek(Calendar.MONDAY);
      cal.setMinimalDaysInFirstWeek(7);
      int week = cal.get(Calendar.WEEK_OF_YEAR);
      weeks.add(week);

    }
    return weeks;
  }

  public static Set<String> getYearMonthList(List<Period> periods) throws ParseException {
    Set<String> months = new HashSet<String>();
    for (Period period : periods) {
      DateTime d1 = new DateTime(period.getStartDate().substring(0, 10));
      DateTime d2 = new DateTime(period.getEndDate().substring(0, 10));
      Calendar cal = Calendar.getInstance();
      cal.setTime(d1.toDate());
      String month1 = String.format("%02d", cal.get(Calendar.MONTH) + 1);
      String ym1 = cal.get(Calendar.YEAR) + month1;
      months.add(ym1);

      cal.setTime(d2.toDate());
      cal.add(Calendar.DAY_OF_MONTH, -1);
      String month2 = String.format("%02d", cal.get(Calendar.MONTH) + 1);
      String ym2 = cal.get(Calendar.YEAR) + month2;
      months.add(ym2);

    }
    return months;
  }

  public static int getNumberOfDaysInMonthForDate(Date d) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
  }
}
