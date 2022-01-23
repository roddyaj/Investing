package com.roddyaj.invest.programs.portfoliomanager.positions;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.roddyaj.invest.html.DataFormatter;
import com.roddyaj.invest.html.Table.Align;
import com.roddyaj.invest.html.Table.Column;
import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.model.Action;
import com.roddyaj.invest.model.Transaction;

public class IncomeCalculator
{
	private final Account account;

	public IncomeCalculator(Account account)
	{
		this.account = account;
	}

	public List<MonthlyIncome> run()
	{
		List<MonthlyIncome> monthToIncome = new ArrayList<>();

		final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM");
		Map<String, Double> monthToOptionsIncome = new HashMap<>();
		Map<String, Double> monthToDividendIncome = new HashMap<>();
		for (Transaction transaction : account.getTransactions())
		{
			if (transaction.getAction() == Action.SELL_TO_OPEN || transaction.getAction() == Action.BUY_TO_CLOSE)
				monthToOptionsIncome.merge(transaction.getDate().format(format), transaction.getAmount(), Double::sum);
			else if (transaction.getAction() == Action.DIVIDEND)
				monthToDividendIncome.merge(transaction.getDate().format(format), transaction.getAmount(), Double::sum);
		}

		Set<String> allMonths = new HashSet<>();
		allMonths.addAll(monthToOptionsIncome.keySet());
		allMonths.addAll(monthToDividendIncome.keySet());
		List<String> sortedMonths = new ArrayList<>(allMonths);
		Collections.sort(sortedMonths, Collections.reverseOrder());
		for (String month : sortedMonths)
			monthToIncome.add(new MonthlyIncome(month, monthToOptionsIncome.getOrDefault(month, 0.), monthToDividendIncome.getOrDefault(month, 0.)));

		return monthToIncome;
	}

	public record MonthlyIncome(String month, double optionsIncome, double dividendIncome)
	{
	}

	public static class MonthlyIncomeFormatter extends DataFormatter<MonthlyIncome>
	{
		public MonthlyIncomeFormatter(Collection<? extends MonthlyIncome> records)
		{
			super("Income", null, records);
		}

		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Month", "%s", Align.L));
			columns.add(new Column("Options", "%.2f", Align.R));
			columns.add(new Column("Dividends", "%.2f", Align.R));
			return columns;
		}

		@Override
		protected List<Object> toRow(MonthlyIncome record)
		{
			return List.of(record.month(), record.optionsIncome(), record.dividendIncome());
		}
	}
}
