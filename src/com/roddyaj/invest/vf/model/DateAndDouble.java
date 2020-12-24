package com.roddyaj.invest.vf.model;

import java.time.LocalDate;

public class DateAndDouble implements Comparable<DateAndDouble>
{
	public final LocalDate date;
	public final double value;

	public DateAndDouble(LocalDate date, double value)
	{
		this.date = date;
		this.value = value;
	}

	@Override
	public String toString()
	{
		return date + " " + value;
	}

	@Override
	public int compareTo(DateAndDouble o)
	{
		return date.compareTo(o.date);
	}
}
