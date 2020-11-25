package com.roddyaj.vf.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Report
{
	public final SymbolData symbolData;

	public boolean pass;

	private final Map<String, String> messages = new LinkedHashMap<>();

	public Report(SymbolData symbolData)
	{
		this.symbolData = symbolData;
	}

	public void addMessage(String key, String message)
	{
		messages.put(key, message);
	}

	@Override
	public String toString()
	{
		List<String> lines = new ArrayList<>();
		try
		{
			String name = symbolData.getName();
			String formattedName = name != null ? name.substring(0, Math.min(30, name.length())) : null;
			lines.add(String.format("%-5s %-30s %7.2f", symbolData.symbol, formattedName, symbolData.getPrice()));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		for (Map.Entry<String, String> entry : messages.entrySet())
			lines.add("      " + entry.getKey() + ": " + entry.getValue());
		return String.join("\n", lines);
	}
}
