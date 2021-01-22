package com.roddyaj.invest.va.model.config;

import java.util.Arrays;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccountSettings
{
	private String name;
	private double annualContrib;
	private Allocation[] allocations;
	private PositionSettings[] positions;
	private AllocationMap allocation = null;

	@JsonProperty("name")
	public String getName()
	{
		return name;
	}

	@JsonProperty("name")
	public void setName(String name)
	{
		this.name = name;
	}

	@JsonProperty("annualContrib")
	public double getAnnualContrib()
	{
		return annualContrib;
	}

	@JsonProperty("annualContrib")
	public void setAnnualContrib(double annualContrib)
	{
		this.annualContrib = annualContrib;
	}

	@JsonProperty("allocations")
	public Allocation[] getAllocations()
	{
		return allocations;
	}

	@JsonProperty("allocations")
	public void setAllocations(Allocation[] allocations)
	{
		this.allocations = allocations;
	}

	@JsonProperty("positions")
	public PositionSettings[] getPositions()
	{
		return positions;
	}

	@JsonProperty("positions")
	public void setPositions(PositionSettings[] positions)
	{
		this.positions = positions;
	}

	public double getAllocation(String symbol)
	{
		if (allocation == null)
			allocation = new AllocationMap(allocations);
		return allocation.getAllocation(symbol);
	}

	public Stream<PositionSettings> getRealPositions()
	{
		return Arrays.stream(positions).filter(p -> !p.getSymbol().startsWith("_"));
	}

	public PositionSettings getPosition(String symbol)
	{
		return Arrays.stream(positions).filter(p -> p.getSymbol().equals(symbol)).findAny().orElse(null);
	}

	public boolean getSell(String symbol)
	{
		boolean sell = false;
		PositionSettings position = getPosition(symbol);
		if (position.getSell() != null)
		{
			sell = position.getSell().booleanValue();
		}
		else
		{
			position = getPosition("_default");
			if (position.getSell() != null)
				sell = position.getSell().booleanValue();
		}
		return sell;
	}

	public String getPeriod(String symbol)
	{
		String period = null;
		PositionSettings position = getPosition(symbol);
		if (position.getPeriod() != null)
		{
			period = position.getPeriod();
		}
		else
		{
			position = getPosition("_default");
			period = position.getPeriod();
		}
		return period;
	}
}
