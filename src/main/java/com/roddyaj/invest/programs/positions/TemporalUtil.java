package com.roddyaj.invest.programs.positions;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashSet;
import java.util.Set;

public final class TemporalUtil
{
	public static final Set<LocalDate> HOLIDAYS = new HashSet<>();
	static
	{
		HOLIDAYS.add(LocalDate.of(2021, 1, 1));
		HOLIDAYS.add(LocalDate.of(2021, 1, 18));
		HOLIDAYS.add(LocalDate.of(2021, 2, 15));
		HOLIDAYS.add(LocalDate.of(2021, 4, 2));
		HOLIDAYS.add(LocalDate.of(2021, 5, 31));
		HOLIDAYS.add(LocalDate.of(2021, 7, 5));
		HOLIDAYS.add(LocalDate.of(2021, 9, 6));
		HOLIDAYS.add(LocalDate.of(2021, 11, 25));
		HOLIDAYS.add(LocalDate.of(2021, 12, 24));
	}

	public static final int ANNUAL_TRADING_DAYS = 252;

	public static final LocalDate END_OF_YEAR = LocalDate.of(2021, 12, 31);

//	public static final Map<String, Integer> TRADING_PERIODS = new HashMap<>();
//	static
//	{
//		TRADING_PERIODS.put("day", 1);
//		TRADING_PERIODS.put("week", 5);
//		TRADING_PERIODS.put("month", 21);
//		TRADING_PERIODS.put("year", ANNUAL_TRADING_DAYS);
//	}
//
//	public static final Map<String, Double> REAL_PERIODS = new HashMap<>();
//	static
//	{
//		REAL_PERIODS.put("day", 1.);
//		REAL_PERIODS.put("week", 7.);
//		REAL_PERIODS.put("month", 30.44);
//		REAL_PERIODS.put("year", 365.);
//	}

	public static boolean isTradingDay(LocalDate date)
	{
		DayOfWeek day = date.getDayOfWeek();
		return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY && !HOLIDAYS.contains(date);
	}

	public static long getDaysApprox(Period period)
	{
		return Math.round(period.getYears() * 365 + period.getMonths() * 30.44 + period.getDays());
	}
}
