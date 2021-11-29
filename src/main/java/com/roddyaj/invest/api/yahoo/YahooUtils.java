package com.roddyaj.invest.api.yahoo;

import com.roddyaj.invest.html.HtmlFormatter;

public final class YahooUtils
{
	private static final String URL = "https://finance.yahoo.com/quote/";

	public static String getLink(String symbol)
	{
		return HtmlFormatter.toLink(getUrl(symbol), symbol);
	}

	public static String getUrl(String symbol)
	{
		return URL + symbol;
	}

	private YahooUtils()
	{
	}
}
