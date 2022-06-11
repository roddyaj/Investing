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
		Map<String, Double> monthToContributions = new HashMap<>();
		for (Transaction transaction : account.getTransactions())
		{
			String dateString = transaction.date().format(format);
			if (transaction.action() == Action.SELL_TO_OPEN || transaction.action() == Action.BUY_TO_CLOSE)
				monthToOptionsIncome.merge(dateString, transaction.amount(), Double::sum);
			else if (transaction.action() == Action.DIVIDEND)
				monthToDividendIncome.merge(dateString, transaction.amount(), Double::sum);
			else if (transaction.action() == Action.TRANSFER)
				monthToContributions.merge(dateString, transaction.amount(), Double::sum);
		}

		Set<String> allMonths = new HashSet<>();
		allMonths.addAll(monthToOptionsIncome.keySet());
		allMonths.addAll(monthToDividendIncome.keySet());
		allMonths.addAll(monthToContributions.keySet());
		List<String> sortedMonths = new ArrayList<>(allMonths);
		Collections.sort(sortedMonths, Collections.reverseOrder());
		for (String month : sortedMonths)
			monthToIncome.add(new MonthlyIncome(month, monthToOptionsIncome.getOrDefault(month, 0.), monthToDividendIncome.getOrDefault(month, 0.),
					monthToContributions.getOrDefault(month, 0.)));

		double optionsTotal = monthToOptionsIncome.values().stream().mapToDouble(Double::doubleValue).sum();
		double dividendTotal = monthToDividendIncome.values().stream().mapToDouble(Double::doubleValue).sum();
		double contributionsTotal = monthToContributions.values().stream().mapToDouble(Double::doubleValue).sum();
		monthToIncome.add(new MonthlyIncome("Total", optionsTotal, dividendTotal, contributionsTotal));

		return monthToIncome;
	}

	public record MonthlyIncome(String month, double optionsIncome, double dividendIncome, double contrib)
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
			columns.add(new Column("Dividend", "%.2f", Align.R));
			columns.add(new Column("Contrib", "%.2f", Align.R));
			return columns;
		}

		@Override
		protected List<Object> toRow(MonthlyIncome record)
		{
			return List.of(record.month(), record.optionsIncome(), record.dividendIncome(), record.contrib());
		}
	}
}
