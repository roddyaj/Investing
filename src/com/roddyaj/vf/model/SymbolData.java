package com.roddyaj.vf.model;

import java.util.ArrayList;
import java.util.List;

public class SymbolData
{
	public final String symbol;
	public String name;

	public double eps;
	public List<Pair<String, Long>> shareholderEquity = new ArrayList<>();
	public double analystTargetPrice;

	public double price;

	public SymbolData(String symbol)
	{
		this.symbol = symbol;
	}
}
