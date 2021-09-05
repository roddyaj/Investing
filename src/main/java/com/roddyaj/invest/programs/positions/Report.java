package com.roddyaj.invest.programs.positions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.roddyaj.invest.model.Position;

public class Report
{
	public final String symbol;

	public final Point p0;

	public final Point p1;

	public final double targetValue;

	public final double targetPct;

	public final Position position;

	public Report(String symbol, Point p0, Point p1, double targetValue, double targetPct, Position position)
	{
		this.symbol = symbol;
		this.p0 = p0;
		this.p1 = p1;
		this.targetValue = targetValue;
		this.targetPct = targetPct;
		this.position = position;
	}

	public static String getHeader()
	{
		return "\n      ------ From -------   ------- To --------   Curr  Target  Delta  Cur %  Tgt %";
	}

	@Override
	public String toString()
	{
		double delta = position.getMarketValue() - targetValue;
//		double currentPct = StringUtils.parsePercent(position.getValue("% Of Account"));
		double currentPct = 0; // TODO
		String dirText = p0 == null || p1 == null ? " " : p1.value > p0.value ? "\033[32m↗\033[0m" : "\033[31m↘\033[0m";
		String p0Text = p0 != null ? p0.toString() : "                   ";
		String p1Text = p1 != null ? p1.toString() : "                   ";
		return String.format("%-5s %s %s %s  %5.0f  %6.0f  %5.0f  %5.2f  %5.2f", symbol, p0Text, dirText, p1Text, position.getMarketValue(),
				targetValue, delta, currentPct, (targetPct * 100));
	}

	public static String toString(Collection<? extends Report> reports)
	{
		List<String> lines = new ArrayList<>();
		lines.add(getHeader());
		for (Report report : reports)
			lines.add(report.toString());
		return String.join("\n", lines);
	}

	public static String toCsvString(Collection<? extends Report> reports)
	{
		return reports.stream().map(r -> String.join(",", List.of(r.symbol, String.format("%.2f", r.targetPct * 100))))
				.collect(Collectors.joining("\n"));
	}
}
