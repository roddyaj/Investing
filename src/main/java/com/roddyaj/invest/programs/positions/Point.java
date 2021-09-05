package com.roddyaj.invest.programs.positions;

import java.time.LocalDate;

public class Point
{
	public final LocalDate date;

	public final double value;

	public Point(LocalDate date, double value)
	{
		this.date = date;
		this.value = value;
	}

	@Override
	public String toString()
	{
		return String.format("%5.0f on %s", value, date);
	}
}
