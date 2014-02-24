package stsc.liquiditator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import stsc.common.Day;
import stsc.common.DayComparator;
import stsc.common.StockInterface;

public class StockFilter {

	static final int minimalDaysWithDataPerLastYear = 216;
	static final int minimalDaysWithDataPerLastMonth = 18;
	static final int minimalAverageYearVolume = 60000000;
	static final float minimalDaysPercentPerLast15Years = (float) 0.9;
	static final int lastYearsAmount = 18;
	static final int daysPerYear = 256;

	static Date today = new Date();

	private static Logger logger = LogManager.getLogger("StockFilter");

	private final DayComparator dayComparator = new DayComparator();

	public StockFilter() {

	}

	public StockFilter(Date testToday) {
		today = testToday;
	}

	public boolean testLastPeriods(StockInterface s) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int date = cal.get(Calendar.DAY_OF_MONTH);

		ArrayList<Day> days = s.getDays();

		Calendar yearAgoCalendar = Calendar.getInstance();
		yearAgoCalendar.set(year - 1, month, date);

		Date yearAgo = yearAgoCalendar.getTime();
		int yearAgoIndex = Collections.binarySearch(days, new Day(yearAgo), dayComparator);
		if (yearAgoIndex < 0)
			yearAgoIndex = -yearAgoIndex;
		int daysWithDataForLastYear = days.size() - yearAgoIndex;
		if (daysWithDataForLastYear < minimalDaysWithDataPerLastYear) {
			logger.debug("stock " + s.getName() + " have only " + daysWithDataForLastYear + " days for last year");
			return false;
		}

		Calendar monthAgoCalendar = Calendar.getInstance();
		monthAgoCalendar.set(year, month, date);
		monthAgoCalendar.add(Calendar.MONTH, -1);

		Date monthAgo = monthAgoCalendar.getTime();

		int monthAgoIndex = Collections.binarySearch(days, new Day(monthAgo), dayComparator);
		if (monthAgoIndex < 0)
			monthAgoIndex = -monthAgoIndex;
		int daysWithDataForLastMonth = days.size() - monthAgoIndex;
		if (daysWithDataForLastMonth < minimalDaysWithDataPerLastMonth) {
			logger.debug("stock " + s.getName() + " have only " + daysWithDataForLastMonth + " days for last month");
			return false;
		}
		double volumeAmount = 0;
		for (int i = daysWithDataForLastYear; i < days.size(); ++i)
			volumeAmount += days.get(i).volume;
		volumeAmount = volumeAmount / daysWithDataForLastYear;

		if (volumeAmount < minimalAverageYearVolume) {
			logger.debug("stock " + s.getName() + " have only " + volumeAmount
					+ ", it is too small average volume amount for last year");
			return false;
		}

		return true;
	}
	
	private boolean testLastNYears(StockInterface s){
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		int year = cal.get(Calendar.YEAR);

		ArrayList<Day> days = s.getDays();
	
		int tenYearsAgoIndex = Collections.binarySearch(days,
				new Day(new DateTime(year - lastYearsAmount, 1, 1, 0, 0).toDate()), dayComparator);
		if (tenYearsAgoIndex < 0)
			tenYearsAgoIndex = -tenYearsAgoIndex;
		int realDaysForTenYears = days.size() - tenYearsAgoIndex;

		int expectedDaysForLast10Year = daysPerYear * lastYearsAmount;

		float averagePercentDaysPer10Year = (float) realDaysForTenYears / expectedDaysForLast10Year;

		if (averagePercentDaysPer10Year < minimalDaysPercentPerLast15Years) {
			logger.debug("stock " + s.getName() + " have only " + realDaysForTenYears + " days per last "
					+ lastYearsAmount + " years, thats not enought");
			return false;
		}
		return true;
	}

	public boolean test(StockInterface s) {
		if (s != null) {
			if (!testLastPeriods(s))
				return false;
			if (!testLastNYears(s))
				return false;
			return true;
		}
		return false;
	}
}