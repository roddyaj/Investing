package com.roddyaj.invest.programs.options;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.util.FileUtils;
import com.roddyaj.invest.util.HtmlFormatter;

public class OptionsOutput
{
	public final List<Position> buyToClose = new ArrayList<>();
	public final List<CallToSell> callsToSell = new ArrayList<>();
	public final List<PutToSell> putsToSell = new ArrayList<>();
	public double availableToTrade;
	public final List<Position> currentPositions = new ArrayList<>();
	public final Map<String, Double> monthToIncome = new HashMap<>();

	// Test code
	public static void main(String[] args)
	{
		OptionsOutput output = new OptionsOutput();
		output.buyToClose.add(new Position("ABC", 50, 123.));
		output.buyToClose.add(new Position("DEF", 50, 123.));
		output.callsToSell.add(new CallToSell(new Position("ABC", 50, 123.), 123., 2));
		output.callsToSell.add(new CallToSell(new Position("DEF", 50, 123.), 123., 2));
		output.putsToSell.add(new PutToSell("ABC", 1000.));
		output.putsToSell.add(new PutToSell("DEF", 100.));
		output.availableToTrade = 2000.;

		Path path = Paths.get(FileUtils.DEFAULT_DIR.toString(), "options.html");
		try
		{
			Files.writeString(path, output.toString());
			Desktop.getDesktop().browse(path.toUri());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public String toString()
	{
		List<String> lines = new ArrayList<>();
		lines.addAll(new Position.OptionHtmlFormatter().toBlock(buyToClose, "Buy To Close"));
		lines.addAll(new CallToSell.CallHtmlFormatter().toBlock(callsToSell, "Calls To Sell"));
		lines.addAll(PutToSell.toBlock(putsToSell, availableToTrade));
		lines.add("<div style=\"padding: 4px 0px;\"></div>");
		lines.addAll(new Position.OptionHtmlFormatter().toBlock(currentPositions, "Current Options (" + currentPositions.size() + ")"));
		var monthlyIncome = monthToIncome.entrySet().stream().sorted((e1, e2) -> e2.getKey().compareTo(e1.getKey())).collect(Collectors.toList());
		lines.addAll(new MonthlyIncomeFormatter().toBlock(monthlyIncome, "Monthly Income"));

		return HtmlFormatter.toDocument("Options", lines);
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
