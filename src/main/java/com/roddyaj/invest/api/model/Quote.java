package com.roddyaj.invest.api.model;

public class Quote
{
	private final double price;
	private final double changePercent;

	public Quote(double price, double changePercent)
	{
		this.price = price;
		this.changePercent = changePercent;
	}

	public double getPrice()
	{
		return price;
	}

	public double getChangePercent()
	{
		return changePercent;
	}
}
