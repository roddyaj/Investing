package com.roddyaj.invest.programs.options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.util.Pair;

public class OptionsOutput
{
	public final List<Position> buyToClose = new ArrayList<>();
	public final List<CallToSell> callsToSell = new ArrayList<>();
	public double availableToTrade;
	public final List<Pair<String, Double>> putsToSell = new ArrayList<>();
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
			lines.add(String.format("%s %.2f", "Available to trade:", availableToTrade));
			for (Pair<String, Double> pair : putsToSell)
				lines.add(String.format("%-4s (%.0f%% return)", pair.left, pair.right));
		}

		lines.add("\n===========================");

		if (!buyToClose.isEmpty())
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

	private static void addHeader(String header, List<String> lines)
	{
		if (!lines.isEmpty())
			lines.add("");
		lines.add(header);
	}
}
