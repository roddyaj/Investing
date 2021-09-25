package com.roddyaj.invest.api.schwab;

import java.time.LocalDate;
import java.util.List;

import com.roddyaj.invest.model.AccountDataSource;
import com.roddyaj.invest.model.OpenOrder;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.Transaction;
import com.roddyaj.invest.model.settings.AccountSettings;

public class SchwabDataSource implements AccountDataSource
{
	private final SchwabPositionsSource positionsSource;
	private final SchwabTransactionsSource transactionsSource;
	private final SchwabOpenOrdersSource openOrdersSource;

	public SchwabDataSource(AccountSettings accountSettings)
	{
		positionsSource = new SchwabPositionsSource(accountSettings);
		transactionsSource = new SchwabTransactionsSource(accountSettings);
		openOrdersSource = new SchwabOpenOrdersSource(accountSettings);
	}

	@Override
	public LocalDate getDate()
	{
		return positionsSource.getDate();
	}

	@Override
	public double getTotalValue()
	{
		return getPositions().stream().filter(p -> p.getSymbol().equals("Account Total")).mapToDouble(Position::getMarketValue).findFirst().orElse(0);
	}

	@Override
	public List<Position> getPositions()
	{
		return positionsSource.getPositions();
	}

	@Override
	public List<Transaction> getTransactions()
	{
		return transactionsSource.getTransactions();
	}

	@Override
	public List<OpenOrder> getOpenOrders()
	{
		return openOrdersSource.getOpenOrders();
	}
}
