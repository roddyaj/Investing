package com.roddyaj.invest.programs.options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.util.HtmlFormatter;

public class OptionsOutput
{
	private final String account;
	public final List<Position> buyToClose = new ArrayList<>();
	public final List<CallToSell> callsToSell = new ArrayList<>();
	public final List<PutToSell> putsToSell = new ArrayList<>();
	public double availableToTrade;
	public final List<Position> currentPositions = new ArrayList<>();
	public final Map<String, Double> monthToIncome = new HashMap<>();

	public OptionsOutput(String account)
	{
		this.account = account;
	}

	@Override
	public String toString()
	{
		final String title = account + " Options";
		return HtmlFormatter.toDocument(title, getContent());
	}

	public List<String> getContent()
	{
		final String title = account + " Options";
		List<String> lines = new ArrayList<>();
		lines.addAll(new Position.OptionHtmlFormatter().toBlock(buyToClose, "Buy To Close"));
		lines.addAll(new CallToSell.CallHtmlFormatter().toBlock(callsToSell, "Calls To Sell"));
		lines.addAll(PutToSell.toBlock(putsToSell, availableToTrade));
		lines.add("<div style=\"padding: 4px 0px;\"></div>");
		lines.addAll(new Position.OptionHtmlFormatter().toBlock(currentPositions, "Current Options (" + currentPositions.size() + ")"));
		var monthlyIncome = monthToIncome.entrySet().stream().sorted((e1, e2) -> e2.getKey().compareTo(e1.getKey())).collect(Collectors.toList());
		lines.addAll(new MonthlyIncomeFormatter().toBlock(monthlyIncome, "Monthly Income"));
		return lines;
	}

	private static class MonthlyIncomeFormatter extends HtmlFormatter<Map.Entry<String, Double>>
	{
		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Month", "%s", Align.L));
			columns.add(new Column("Income", "$%.2f", Align.R));
			return columns;
		}

		@Override
		protected List<Object> getObjectElements(Map.Entry<String, Double> e)
		{
			return List.of(e.getKey(), e.getValue());
		}
	}
}
