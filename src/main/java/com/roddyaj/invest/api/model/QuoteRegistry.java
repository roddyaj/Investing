package com.roddyaj.invest.api.model;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class QuoteRegistry
{
	private final Map<String, QuoteProvider> providers = new LinkedHashMap<>();

	public void addProvider(QuoteProvider provider)
	{
		providers.put(provider.getName(), provider);
	}

	public QuoteProvider getProvider(String name)
	{
		return providers.get(name);
	}

	public Quote getQuote(String symbol)
	{
		Quote quote = null;
		for (QuoteProvider provider : providers.values())
		{
			try
			{
				quote = provider.getQuote(symbol);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			if (quote != null)
				break;
		}
		return quote;
	}

	public Double getPrice(String symbol)
	{
		Quote quote = getQuote(symbol);
		return quote != null ? quote.getPrice() : null;
	}
}
