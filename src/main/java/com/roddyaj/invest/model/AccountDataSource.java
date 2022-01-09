package com.roddyaj.invest.model;

import java.time.ZonedDateTime;
import java.util.List;

public interface AccountDataSource
{
	ZonedDateTime getDateTime();

	double getTotalValue();

	List<Position> getPositions();

	List<Transaction> getTransactions();

	List<OpenOrder> getOpenOrders();
}
