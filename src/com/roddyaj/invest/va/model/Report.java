package com.roddyaj.invest.va.model;

public class Report
{
	private final String symbol;

	private final Point p0;

	private final Point p1;

	private final double targetValue;

	private final double actualValue;

	private final double targetPct;

	public Report(String symbol, Point p0, Point p1, double targetValue, double actualValue, double targetPct)
	{
		this.symbol = symbol;
		this.p0 = p0;
		this.p1 = p1;
		this.targetValue = targetValue;
		this.actualValue = actualValue;
		this.targetPct = targetPct;
	}

	public static String getHeader()
	{
		return "\n      ------ From -------  ------- To --------   Curr  Target  Delta  Tgt %";
	}

	@Override
	public String toString()
	{
		double delta = actualValue - targetValue;
		return String.format("%-5s %s  %s  %5.0f  %6.0f  %5.0f  %5.2f", symbol, p0, p1, actualValue, targetValue, delta, (targetPct * 100));
	}
}
