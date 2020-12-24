package com.roddyaj.invest.vf.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.roddyaj.invest.vf.model.Result;
import com.roddyaj.invest.vf.model.Results;
import com.roddyaj.invest.vf.model.SymbolData;
import com.roddyaj.invest.vf.model.SymbolResult;

public class Report
{
	public static void print(Results results)
	{
		System.out.println(toString(results));
	}

	private static String toString(Results results)
	{
		Collections.sort(results.fails, (a, b) -> Double.compare(a.sortValue, b.sortValue));
		Collections.sort(results.passes, (a, b) -> Double.compare(a.sortValue, b.sortValue));

		List<String> lines = new ArrayList<>();
		format(results.fails, "Fail", lines);
		format(results.passes, "Pass", lines);
		return String.join("\n", lines);
	}

	private static void format(Collection<? extends SymbolResult> results, String title, Collection<? super String> lines)
	{
		if (!results.isEmpty())
		{
			lines.add("======================== " + title + " ========================\n");
			for (SymbolResult result : results)
			{
				lines.add(toString(result));
				if (result.hasResults())
					lines.add("");
			}
		}
	}

	private static String toString(SymbolResult result)
	{
		List<String[]> rows = new ArrayList<>();

		SymbolData data = result.data;
		String stockInfo = String.format("%-5s %-35s", data.symbol, limit(data.getNameIfPresent(), 35));
		String price = String.format("%.2f", data.getPriceIfPresent());
		rows.add(new String[] { stockInfo, price });

		for (Result subResult : result.results)
		{
			String status = subResult.pass != null ? (subResult.pass ? "Pass" : "Fail") : "";
			String message = String.format("  %4s %s", status, subResult.property);
			String value = subResult.value != null ? subResult.value.toString() : "";
			rows.add(new String[] { message, value });
		}

		return String.join("\n", formatTable(rows));
	}

	private static String limit(String s, int size)
	{
		return s != null && size < s.length() ? s.substring(0, size) : s;
	}

	private static List<String> formatTable(Collection<? extends Object[]> data)
	{
		List<String> lines = new ArrayList<>();

		List<Integer> columnSizes = new ArrayList<>();
		for (Object[] row : data)
		{
			int c = 0;
			for (Object column : row)
			{
				if (c < columnSizes.size())
					columnSizes.set(c, Math.max(columnSizes.get(c), column.toString().length()));
				else
					columnSizes.add(column.toString().length());
				c++;
			}
		}

		StringBuilder formatBuilder = new StringBuilder();
		for (Integer colSize : columnSizes)
		{
			if (formatBuilder.length() > 0)
				formatBuilder.append(' ');
			formatBuilder.append("%-").append(colSize.intValue()).append('s');
		}
		String format = formatBuilder.toString();

		for (Object[] row : data)
			lines.add(String.format(format, row));

		return lines;
	}
}
