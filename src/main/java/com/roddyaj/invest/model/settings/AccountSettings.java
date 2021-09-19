package com.roddyaj.invest.model.settings;

import java.util.Arrays;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.roddyaj.invest.model.Output;

public class AccountSettings
{
	private String name;
	private String accountNumber;
	private double maxPosition;
	private double startingBalance;
	private Allocation[] allocations;
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

	@JsonProperty("accountNumber")
	public String getAccountNumber()
	{
		return accountNumber;
	}

	@JsonProperty("accountNumber")
	public void setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
	}

	@JsonProperty("maxPosition")
	public double getMaxPosition()
	{
		return maxPosition;
	}

	@JsonProperty("maxPosition")
	public void setMaxPosition(double maxPosition)
	{
		this.maxPosition = maxPosition;
	}

	@JsonProperty("startingBalance")
	public double getStartingBalance()
	{
		return startingBalance;
	}

	@JsonProperty("startingBalance")
	public void setStartingBalance(double startingBalance)
	{
		this.startingBalance = startingBalance;
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

	public Stream<String> allocationStream()
	{
		return Arrays.stream(allocations).map(Allocation::getCatLastToken).filter(s -> s.toUpperCase().equals(s)).distinct();
	}

	public boolean hasAllocation(String symbol)
	{
		return Arrays.stream(allocations).anyMatch(a -> a.getCatLastToken().equals(symbol));
	}

	public double getAllocation(String symbol)
	{
		return allocation.getAllocation(symbol);
	}
}
