package com.roddyaj.invest.va.model;

public class Report
{
	private final String symbol;

	private final Point p0;

	private final Point p1;

	private final double targetValue;

	private final double actualValue;

	public Report(String symbol, Point p0, Point p1, double targetValue, double actualValue)
	{
		this.symbol = symbol;
		this.p0 = p0;
		this.p1 = p1;
		this.targetValue = targetValue;
		this.actualValue = actualValue;
	}

	public static String getHeader()
	{
		return "\n      -------- From --------  --------- To ---------  TodayTgt   Current    Delta";
	}

	@Override
	public String toString()
	{
		double delta = actualValue - targetValue;
		return String.format("%-5s %s  %s  %8.2f  %8.2f  %7.2f", symbol, p0, p1, targetValue, actualValue, delta);
	}
}
