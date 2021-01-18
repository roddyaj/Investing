package com.roddyaj.invest.va.model;

import java.util.Map;

public class Position
{
	private double value;

	private double price;

	private Map<String, String> data;

	public double getMarketValue()
	{
		return value;
	}

	public void setMarketValue(double value)
	{
		this.value = value;
	}

	public double getPrice()
	{
		return price;
	}

	public void setPrice(double price)
	{
		this.price = price;
	}

	public void setValues(Map<String, String> data)
	{
		this.data = data;
	}

	public String getValue(String key)
	{
		return data.get(key);
	}
}
