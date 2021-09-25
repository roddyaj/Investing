package com.roddyaj.invest.programs.portfoliomanager.positions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.roddyaj.invest.model.AccountDataSource;
import com.roddyaj.invest.model.OpenOrder;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.Transaction;

public class TestDataSource implements AccountDataSource
{
	private final List<Position> positions = new ArrayList<>();

	@Override
	public LocalDate getDate()
	{
		return null;
	}

	@Override
	public double getTotalValue()
	{
		return 0;
	}

	@Override
	public List<Position> getPositions()
	{
		return positions;
	}

	@Override
	public List<Transaction> getTransactions()
	{
		return List.of();
	}

	@Override
	public List<OpenOrder> getOpenOrders()
	{
		return List.of();
	}
}
