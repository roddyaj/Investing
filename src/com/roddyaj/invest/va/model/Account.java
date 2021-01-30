package com.roddyaj.invest.va.model;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Account
{
	public LocalDate date;

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

	public Collection<Position> getPositions()
	{
		return positions.values();
	}

	public void addPosition(String symbol, Position position)
	{
		positions.put(symbol, position);
	}
}
