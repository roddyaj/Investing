package com.roddyaj.invest.va.model.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccountSettings
{
	private String name;
	private double annualContrib;
	private Allocation[] allocations;
	private PositionSettings[] positions;

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
}
