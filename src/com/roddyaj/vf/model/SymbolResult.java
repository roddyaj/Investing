package com.roddyaj.vf.model;

import java.io.IOException;
import java.util.ArrayList;
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
		List<String> lines = new ArrayList<>();

		try
		{
			lines.add(String.format("%-5s %-30s %7.2f", data.symbol, limit(data.getName(), 30), data.getPrice()));
		}
		catch (IOException e)
		{
			lines.add(e.getMessage() + ": " + data.symbol);
		}
		for (Result result : results)
		{
			String message = limit(result.message, 29);
			String success = result.pass ? "Pass" : "Fail";
			String price = result.price != null ? String.format("%7.2f", result.price.doubleValue()) : "----";
			lines.add(String.format("  %-29s %s %7s", message, success, price));
		}

		return String.join("\n", lines);
	}

	private String limit(String s, int size)
	{
		return size < s.length() ? s.substring(0, size) : s;
	}
}
