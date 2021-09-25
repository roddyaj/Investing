package com.roddyaj.invest.model;

import java.time.LocalDate;
import java.util.List;

public interface AccountDataSource
{
	LocalDate getDate();

	double getTotalValue();

	List<Position> getPositions();

	List<Transaction> getTransactions();

	List<OpenOrder> getOpenOrders();
}
