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

import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.util.FileUtils;

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
			Files.writeString(path, output.toHtmlString());
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

		if (!buyToClose.isEmpty())
		{
			addHeader("Buy To Close:", lines);
			buyToClose.forEach(p -> lines.add(p.toString()));
		}

		if (!callsToSell.isEmpty())
		{
			addHeader("Sell Calls:", lines);
			callsToSell.forEach(c -> lines.add(c.toString()));
		}

		if (!putsToSell.isEmpty())
		{
			addHeader("Sell Puts:", lines);
			lines.add(String.format("%s $%.2f", "Available to trade:", availableToTrade));
			putsToSell.forEach(p -> lines.add(p.toString()));
		}

		if (!lines.isEmpty())
			lines.add("\n===========================");

		if (!currentPositions.isEmpty())
		{
			addHeader("Current Options:", lines);
			currentPositions.forEach(p -> lines.add(p.toString()));
		}

		if (!monthToIncome.isEmpty())
		{
			addHeader("Monthly Income:", lines);
			monthToIncome.entrySet().stream().sorted((e1, e2) -> e2.getKey().compareTo(e1.getKey()))
					.forEach(e -> lines.add(String.format("%s %7.2f", e.getKey(), e.getValue())));
		}

		return String.join("\n", lines);
	}

	public String toHtmlString()
	{
		List<String> lines = new ArrayList<>();
		lines.add("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n<title>Options</title>");
		lines.add("<style>");
		lines.add("th, td { padding: 2px 4px; }");
		lines.add(".heading { margin-bottom: 4px; }");
		lines.add(".block { border-style: solid; border-width: 1px; padding: 4px; margin: 8px 0px; }");
		lines.add("</style>");
		lines.add("</head>\n<body>");

		lines.addAll(new Position.OptionHtmlFormatter().toBlock(buyToClose, "Buy To Close"));
		lines.addAll(new CallToSell.CallHtmlFormatter().toBlock(callsToSell, "Calls To Sell"));
		lines.addAll(PutToSell.toBlock(putsToSell, availableToTrade));
		lines.add("<div style=\"padding: 4px 0px;\"></div>");
		lines.addAll(new Position.OptionHtmlFormatter().toBlock(currentPositions, "Current Options"));

//		if (!monthToIncome.isEmpty())
//		{
//			addHeader("Monthly Income:", lines);
//			monthToIncome.entrySet().stream().sorted((e1, e2) -> e2.getKey().compareTo(e1.getKey()))
//					.forEach(e -> lines.add(String.format("%s %7.2f", e.getKey(), e.getValue())));
//		}

		lines.add("</body>\n</html>");
		return String.join("\n", lines);
	}

	private static void addHeader(String header, List<String> lines)
	{
		if (!lines.isEmpty())
			lines.add("");
		lines.add(header);
	}
}
