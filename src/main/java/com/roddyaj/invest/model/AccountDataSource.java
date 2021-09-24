package com.roddyaj.invest.model;

import java.time.LocalDate;
import java.util.List;

public interface AccountDataSource
{
	LocalDate getDate();

	List<Position> getPositions();

	List<Transaction> getTransactions();

	List<OpenOrder> getOpenOrders();
}
