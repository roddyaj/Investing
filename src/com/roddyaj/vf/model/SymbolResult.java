package com.roddyaj.vf.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SymbolResult
{
	public final SymbolData data;

	public boolean pass;

	private final List<Result> results = new ArrayList<>();

	public SymbolResult(SymbolData data)
	{
		this.data = data;
	}

	public void addResult(Result result)
	{
		results.add(result);
	}

	@Override
	public String toString()
	{
		List<String[]> rows = new ArrayList<>();

		String stockInfo = String.format("%-5s %-40s", data.symbol, limit(data.getNameIfPresent(), 40));
		String price = formatPrice(data.getPriceIfPresent());
		rows.add(new String[] { stockInfo, price });

		for (Result result : results)
		{
			String message = String.format("  %s %s", result.pass ? "Pass" : "Fail", result.message);
			price = result.price != null ? formatPrice(result.price.doubleValue()) : "   ----";
			rows.add(new String[] { message, price });
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

	private static String formatPrice(double price)
	{
		return String.format("%7.2f", price);
	}
}
