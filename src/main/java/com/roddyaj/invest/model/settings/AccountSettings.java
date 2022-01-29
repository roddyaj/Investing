package com.roddyaj.invest.model.settings;

import java.util.Arrays;
import java.util.stream.Stream;

public class AccountSettings
{
	private String name;
	private String accountNumber;
	private double maxPosition;
	private double minOrder;
	private double startingBalance;
	private boolean optionsEnabled = true;
	private Allocation[] allocations;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getAccountNumber()
	{
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
	}

	public double getMaxPosition()
	{
		return maxPosition;
	}

	public void setMaxPosition(double maxPosition)
	{
		this.maxPosition = maxPosition;
	}

	public double getMinOrder()
	{
		return minOrder;
	}

	public void setMinOrder(double minOrder)
	{
		this.minOrder = minOrder;
	}

	public double getStartingBalance()
	{
		return startingBalance;
	}

	public void setStartingBalance(double startingBalance)
	{
		this.startingBalance = startingBalance;
	}

	public boolean isOptionsEnabled()
	{
		return optionsEnabled;
	}

	public void setOptionsEnabled(boolean optionsEnabled)
	{
		this.optionsEnabled = optionsEnabled;
	}

	public Allocation[] getAllocations()
	{
		return allocations;
	}

	public void setAllocations(Allocation[] allocations)
	{
		this.allocations = allocations;
	}

	public Stream<String> allocationStream()
	{
		return allocations == null ? Stream.of()
				: Arrays.stream(allocations).map(Allocation::getCatLastToken).filter(s -> s.toUpperCase().equals(s)).distinct();
	}

	public boolean hasAllocation(String symbol)
	{
		return allocations != null && Arrays.stream(allocations).anyMatch(a -> a.getCatLastToken().equals(symbol));
	}
}
