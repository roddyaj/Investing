package com.roddyaj.vf.model;

public class Report
{
	public final SymbolData symbolData;

	public final boolean pass;

	public Report(SymbolData symbolData, boolean pass)
	{
		this.symbolData = symbolData;
		this.pass = pass;
	}

	@Override
	public String toString()
	{
		String name = symbolData.name.substring(0, Math.min(30, symbolData.name.length()));
		return String.format("%-5s %-30s %7.2f", symbolData.symbol, name, symbolData.price);
	}
}
