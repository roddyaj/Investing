package com.roddyaj.invest.programs.portfoliomanager.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.roddyaj.invest.html.Block;
import com.roddyaj.invest.html.Table;
import com.roddyaj.invest.html.Table.Align;
import com.roddyaj.invest.html.Table.Column;
import com.roddyaj.invest.model.AbstractOutput;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.util.HtmlFormatter;

public class OptionsOutput extends AbstractOutput
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

	@Override
	public List<String> getContent()
	{
		List<String> lines = new ArrayList<>();
		lines.addAll(getActionsHtml());
		lines.addAll(getCurrentOptionsHtml());
		lines.addAll(getIncomeHtml());
		return lines;
	}

	public List<String> getActionsHtml()
	{
		List<String> lines = new ArrayList<>();

		String info = String.format("Frees up $%.0f", buyToClose.stream().filter(Position::isPutOption).mapToDouble(Position::getMoneyInPlay).sum());
		lines.addAll(Position.OptionHtmlFormatter.toBlock(buyToClose, "Buy To Close", info).toHtml());

		Collections.sort(callsToSell);
		lines.addAll(CallToSell.CallHtmlFormatter.toBlock(callsToSell).toHtml());

		Collections.sort(putsToSell);
		lines.addAll(PutToSell.PutHtmlFormatter.toBlock(putsToSell, availableToTrade).toHtml());

		return lines;
	}

	public List<String> getCurrentOptionsHtml()
	{
		List<String> lines = new ArrayList<>();

		long callsCount = currentPositions.stream().filter(Position::isCallOption).count();
		double callsInPlay = currentPositions.stream().filter(Position::isCallOption).mapToDouble(Position::getMoneyInPlay).sum();
		long putsCount = currentPositions.stream().filter(Position::isPutOption).count();
		double putsInPlay = currentPositions.stream().filter(Position::isPutOption).mapToDouble(Position::getMoneyInPlay).sum();
		String info = String.format("C: %d $%.0f &nbsp;P: %d $%.0f &nbsp;T: %d $%.0f", callsCount, callsInPlay, putsCount, putsInPlay,
				callsCount + putsCount, callsInPlay + putsInPlay);
		lines.addAll(Position.OptionHtmlFormatter.toBlock(currentPositions, "Current Options", info).toHtml());

		return lines;
	}

	public List<String> getIncomeHtml()
	{
		List<String> lines = new ArrayList<>();

		var monthlyIncome = monthToIncome.entrySet().stream().sorted((e1, e2) -> e2.getKey().compareTo(e1.getKey())).collect(Collectors.toList());
		lines.addAll(MonthlyIncomeFormatter.toBlock(monthlyIncome).toHtml());

		return lines;
	}

	private static class MonthlyIncomeFormatter
	{
		public static Block toBlock(Collection<? extends Map.Entry<String, Double>> income)
		{
			Table table = new Table(getColumns(), getRows(income));
			return new Block("Options Income", null, table);
		}

		private static List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Month", "%s", Align.L));
			columns.add(new Column("Income", "$%.2f", Align.R));
			return columns;
		}

		private static List<List<Object>> getRows(Collection<? extends Map.Entry<String, Double>> income)
		{
			return income.stream().map(MonthlyIncomeFormatter::toRow).collect(Collectors.toList());
		}

		private static List<Object> toRow(Map.Entry<String, Double> e)
		{
			return List.of(e.getKey(), e.getValue());
		}
	}
}
