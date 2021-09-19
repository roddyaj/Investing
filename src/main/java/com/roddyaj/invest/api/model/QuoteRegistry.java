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
		Double price = null;
		Double changePercent = null;
		for (QuoteProvider provider : providers.values())
		{
			try
			{
				Quote quote = provider.getQuote(symbol);
				if (quote != null)
				{
					if (price == null)
						price = quote.getPrice();
					if (changePercent == null)
						changePercent = quote.getChangePercent();

					if (price != null && changePercent != null)
						break;
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return price != null || changePercent != null ? new Quote(price != null ? price : 0, changePercent != null ? changePercent : 0) : null;
	}

	public Double getPrice(String symbol)
	{
		Quote quote = getQuote(symbol);
		return quote != null ? quote.getPrice() : null;
	}
}
