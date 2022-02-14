package com.roddyaj.invest.model;

import java.util.ArrayList;
import java.util.List;

public class CompletePosition
{
	private final Position position;

	private final List<Transaction> transactions = new ArrayList<>();

	private final List<OpenOrder> openOrders = new ArrayList<>();

	public CompletePosition(Position position)
	{
		this.position = position;
	}

	public Position getPosition()
	{
		return position;
	}

	public List<Transaction> getTransactions()
	{
		return transactions;
	}

	public List<OpenOrder> getOpenOrders()
	{
		return openOrders;
	}
}
