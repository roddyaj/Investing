package com.roddyaj.vf.api.alphavantage;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.roddyaj.vf.model.SymbolData;
import com.roddyaj.vf.model.SymbolData.BalanceSheet;
import com.roddyaj.vf.model.SymbolData.IncomeStatement;
import com.roddyaj.vf.request.RequestCache;

public class AlphaVantageAPI
{
	private static final String urlRoot = "https://www.alphavantage.co/query?";

	private final String urlBase;

	private final RequestCache cache;

	public AlphaVantageAPI(JSONObject settings) throws IOException
	{
		String apiKey = (String)settings.get("alphavantage.apiKey");
		long sleepTime = Duration.parse((String)settings.get("alphavantage.sleep")).toMillis();
		if (apiKey == null)
			throw new IOException("Error: No API key specified");

		urlBase = new StringBuilder(urlRoot).append("apikey=").append(apiKey).toString();
		cache = new RequestCache(sleepTime, settings);
	}

	public void requestData(SymbolData data) throws IOException
	{
		JSONObject json;

		json = getOverview(data.symbol);
		data.name = getString(json, "Name");
		data.eps = getDouble(json, "EPS");
		data.analystTargetPrice = getDouble(json, "AnalystTargetPrice");

		json = getIncomeStatement(data.symbol);
		JSONArray annualReports = (JSONArray)json.get("annualReports");
		for (Object r : annualReports)
		{
			JSONObject report = (JSONObject)r;
			IncomeStatement incomeStatement = new IncomeStatement();
			incomeStatement.period = getString(report, "fiscalDateEnding");
			incomeStatement.incomeBeforeTax = getLong(report, "incomeBeforeTax");
			incomeStatement.operatingIncome = getLong(report, "operatingIncome");
			incomeStatement.taxProvision = getLong(report, "taxProvision");
			data.incomeStatements.add(incomeStatement);
		}

		json = getBalanceSheet(data.symbol);
		annualReports = (JSONArray)json.get("annualReports");
		for (Object r : annualReports)
		{
			JSONObject report = (JSONObject)r;
			BalanceSheet balanceSheet = new BalanceSheet();
			balanceSheet.period = getString(report, "fiscalDateEnding");
			balanceSheet.totalShareholderEquity = getLong(report, "totalShareholderEquity");
			balanceSheet.shortTermDebt = getLong(report, "shortTermDebt");
			balanceSheet.longTermDebt = getLong(report, "longTermDebt");
			data.balanceSheets.add(balanceSheet);
		}

		json = getQuote(data.symbol);
		JSONObject quote = (JSONObject)json.get("Global Quote");
		data.price = getDouble(quote, "05. price");
	}

	public JSONObject getOverview(String symbol) throws IOException
	{
		return getAsJSON(symbol, "OVERVIEW");
	}

	public JSONObject getIncomeStatement(String symbol) throws IOException
	{
		return getAsJSON(symbol, "INCOME_STATEMENT");
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

	private static String getString(JSONObject obj, String key)
	{
		return (String)obj.get(key);
	}

	private static long getLong(JSONObject obj, String key)
	{
		String value = (String)obj.get(key);
		return "None".equals(value) ? 0 : Long.parseLong(value);
	}

	private static double getDouble(JSONObject obj, String key)
	{
		return Double.parseDouble((String)obj.get(key));
	}
}
