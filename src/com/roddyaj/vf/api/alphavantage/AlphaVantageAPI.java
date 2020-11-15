package com.roddyaj.vf.api.alphavantage;

import java.io.IOException;
import java.net.URI;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.roddyaj.vf.model.Pair;
import com.roddyaj.vf.model.SymbolData;
import com.roddyaj.vf.request.RequestCache;

public class AlphaVantageAPI
{
	private static final String urlRoot = "https://www.alphavantage.co/query?";

	private final String urlBase;

	private final RequestCache cache;

	public AlphaVantageAPI(String apiKey)
	{
		urlBase = new StringBuilder(urlRoot).append("apikey=").append(apiKey).toString();
		cache = new RequestCache();
	}

	public SymbolData requestData(String symbol) throws IOException
	{
		SymbolData data = new SymbolData();
		JSONObject json;

		json = getOverview(symbol);
		data.eps = Double.parseDouble((String)json.get("EPS"));
		data.analystTargetPrice = Double.parseDouble((String)json.get("AnalystTargetPrice"));

		json = getBalanceSheet(symbol);
		JSONArray annualReports = (JSONArray)json.get("annualReports");
		for (Object r : annualReports)
		{
			JSONObject report = (JSONObject)r;
			String periodEnding = (String)report.get("fiscalDateEnding");
			long equity = Long.parseLong((String)report.get("totalShareholderEquity"));
			data.shareholderEquity.add(new Pair<>(periodEnding, equity));
		}

		json = getQuote(symbol);
		JSONObject quote = (JSONObject)json.get("Global Quote");
		data.price = Double.parseDouble((String)quote.get("05. price"));

		return data;
	}

	public JSONObject getOverview(String symbol) throws IOException
	{
		return getAsJSON(symbol, "OVERVIEW");
	}

	public JSONObject getBalanceSheet(String symbol) throws IOException
	{
		return getAsJSON(symbol, "BALANCE_SHEET");
	}

	public JSONObject getQuote(String symbol) throws IOException
	{
		return getAsJSON(symbol, "GLOBAL_QUOTE");
	}

	private JSONObject getAsJSON(String symbol, String function) throws IOException
	{
		String url = new StringBuilder(urlBase).append("&function=").append(function).append("&symbol=").append(symbol).toString();
		String cacheKey = new StringBuilder("AV_").append(symbol).append('_').append(function).toString();
		return cache.getJson(URI.create(url), cacheKey);
	}
}
