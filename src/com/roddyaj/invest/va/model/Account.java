package com.roddyaj.invest.va.model;

import java.util.HashMap;
import java.util.Map;

public class Account
{
	private double totalValue;

	private final Map<String, Position> positions = new HashMap<>();

	public double getTotalValue()
	{
		return totalValue;
	}

	public void setTotalValue(double totalValue)
	{
		this.totalValue = totalValue;
	}

	public boolean hasSymbol(String symbol)
	{
		return positions.containsKey(symbol);
	}

	public Position getPosition(String symbol)
	{
		return positions.get(symbol);
	}

	public void addPosition(String symbol, Position position)
	{
		positions.put(symbol, position);
	}
}
