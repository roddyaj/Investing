package com.roddyaj.invest.programs.options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.roddyaj.invest.model.Position;

public class OptionsOutput
{
	public final List<Position> buyToClose = new ArrayList<>();
	public final List<CallToSell> callsToSell = new ArrayList<>();
	public final List<PutToSell> putsToSell = new ArrayList<>();
	public double availableToTrade;
	public final List<Position> currentPositions = new ArrayList<>();
	public final Map<String, Double> monthToIncome = new HashMap<>();

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
		lines.add("<!DOCTYPE html>\n<html>\n<body>");

//		if (!buyToClose.isEmpty())
//		{
//			addHeader("Buy To Close:", lines);
//			buyToClose.forEach(p -> lines.add(p.toHtmlString()));
//		}

		lines.addAll(CallToSell.toHtml(callsToSell));
		lines.add(String.format("<h4>%s $%.2f</h4>", "Available to trade:", availableToTrade));
		lines.addAll(PutToSell.toHtml(putsToSell));

//		if (!putsToSell.isEmpty())
//		{
//			addHeader("Sell Puts:", lines);
//			lines.add(String.format("%s %.2f", "Available to trade:", availableToTrade));
//			putsToSell.forEach(p -> lines.add(p.toString()));
//		}
//
//		if (!lines.isEmpty())
//			lines.add("\n===========================");
//
//		if (!currentPositions.isEmpty())
//		{
//			addHeader("Current Options:", lines);
//			currentPositions.forEach(p -> lines.add(p.toString()));
//		}
//
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
