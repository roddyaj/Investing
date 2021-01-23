package com.roddyaj.invest.va.model;

public class Report
{
	private final String symbol;

	private final Point p0;

	private final Point p1;

	private final double targetValue;

	private final double targetPct;

	private final Position position;

	public Report(String symbol, Point p0, Point p1, double targetValue, double targetPct, Position position)
	{
		this.symbol = symbol;
		this.p0 = p0;
		this.p1 = p1;
		this.targetValue = targetValue;
		this.targetPct = targetPct;
		this.position = position;
	}

	public static String getHeader()
	{
		return "\n      ------ From -------  ------- To --------   Curr  Target  Delta  Cur %  Tgt %";
	}

	@Override
	public String toString()
	{
		double delta = position.getMarketValue() - targetValue;
		double currentPct = Double.parseDouble(position.getValue("% Of Account").replace("%", ""));
		return String.format("%-5s %s  %s  %5.0f  %6.0f  %5.0f  %5.2f  %5.2f", symbol, p0, p1, position.getMarketValue(), targetValue, delta,
				currentPct, (targetPct * 100));
	}
}
