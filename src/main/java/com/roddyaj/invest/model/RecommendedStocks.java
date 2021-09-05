package com.roddyaj.invest.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RecommendedStocks
{
	public final String source;
	public final Collection<String> tickers;

	public RecommendedStocks(String source, Collection<String> tickers)
	{
		this.source = source;
		this.tickers = tickers;
	}

	public String toYahooCsv()
	{
		List<String> copy = new ArrayList<>(tickers);
		Collections.reverse(copy);
		return copy.stream().collect(Collectors.joining(","));
	}
}
