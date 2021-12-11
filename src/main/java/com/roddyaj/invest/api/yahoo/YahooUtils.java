package com.roddyaj.invest.api.yahoo;

import java.util.Map;

import com.roddyaj.invest.html.HtmlUtils;

public final class YahooUtils
{
	private static final String URL = "https://finance.yahoo.com/quote/";

	private static final String ICON_URL = "https://s.yimg.com/cv/apiv2/default/icons/favicon_y19_32x32_custom.svg";

	public static String getLink(String symbol)
	{
		return HtmlUtils.toLink(getUrl(symbol), symbol);
	}

	public static String getIconLink(String symbol)
	{
		return HtmlUtils.toLink(getUrl(symbol), HtmlUtils.tag("img", Map.of("src", ICON_URL, "width", 14, "height", 14)));
	}

	public static String getUrl(String symbol)
	{
		return URL + symbol;
	}

	private YahooUtils()
	{
	}
}
