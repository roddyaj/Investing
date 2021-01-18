package com.roddyaj.invest.va.model;

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
		return "Point [" + date + ", " + value + "]";
	}
}
