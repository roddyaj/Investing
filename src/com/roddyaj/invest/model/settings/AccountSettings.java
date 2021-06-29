package com.roddyaj.invest.model.settings;

import java.util.Arrays;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.roddyaj.invest.model.Output;

public class AccountSettings
{
	private String name;
	private String alias;
	private double annualContrib;
	private Allocation[] allocations;
	private PositionSettings[] positions;
	private AllocationMap allocation;

	public void createMap(double untrackedPercent, Output output)
	{
		allocation = new AllocationMap(allocations, untrackedPercent, output);
	}

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

	@JsonProperty("alias")
	public String getAlias()
	{
		return alias;
	}

	@JsonProperty("alias")
	public void setAlias(String alias)
	{
		this.alias = alias;
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

	public boolean hasAllocation(String symbol)
	{
		return Arrays.stream(allocations).anyMatch(a -> a.getCatLastToken().equals(symbol));
	}

	public double getAllocation(String symbol)
	{
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
