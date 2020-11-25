package com.roddyaj.vf.model;

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
		String name = symbolData.name != null ? symbolData.name.substring(0, Math.min(30, symbolData.name.length())) : null;
		lines.add(String.format("%-5s %-30s %7.2f", symbolData.symbol, name, symbolData.price));
		for (Map.Entry<String, String> entry : messages.entrySet())
			lines.add("      " + entry.getKey() + ": " + entry.getValue());
		return String.join("\n", lines);
	}
}
