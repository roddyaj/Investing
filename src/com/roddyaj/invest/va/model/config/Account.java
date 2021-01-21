package com.roddyaj.invest.va.model.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Account
{
	private String name;
	private double annualContrib;
	private Allocation[] allocations;
	private Position[] positions;

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
	public Position[] getPositions()
	{
		return positions;
	}

	@JsonProperty("positions")
	public void setPositions(Position[] positions)
	{
		this.positions = positions;
	}
}
