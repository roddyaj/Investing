package com.roddyaj.invest.api.model;

public class Quote
{
	private final double price;
	private final Double changePercent;

	public Quote(double price, Double changePercent)
	{
		this.price = price;
		this.changePercent = changePercent;
	}

	public double getPrice()
	{
		return price;
	}

	public Double getChangePercent()
	{
		return changePercent;
	}
}
