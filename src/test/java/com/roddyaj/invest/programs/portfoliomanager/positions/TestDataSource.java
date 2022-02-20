package com.roddyaj.invest.programs.portfoliomanager.positions;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.roddyaj.invest.model.AbstractDataSource;
import com.roddyaj.invest.model.OpenOrder;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.Transaction;

public class TestDataSource extends AbstractDataSource
{
	private final List<Position> positions = new ArrayList<>();

	@Override
	public ZonedDateTime getDateTime()
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
