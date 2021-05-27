package com.roddyaj.invest.programs.va.model;

import java.util.Map;

public class Position
{
	public final String symbol;

	private double value;

	private double price;

	private Map<String, String> data;

	public Position(String symbol)
	{
		this.symbol = symbol;
	}

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

	@Override
	public String toString()
	{
		return "Position [symbol=" + symbol + ", value=" + value + ", price=" + price + ", data=" + data + "]";
	}
}
