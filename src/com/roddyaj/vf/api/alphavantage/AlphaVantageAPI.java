package com.roddyaj.vf.api.alphavantage;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.roddyaj.vf.model.DateAndDouble;
import com.roddyaj.vf.model.SymbolData;
import com.roddyaj.vf.model.SymbolData.BalanceSheet;
import com.roddyaj.vf.model.SymbolData.DataRequester;
import com.roddyaj.vf.model.SymbolData.IncomeStatement;
import com.roddyaj.vf.request.RequestCache;

public class AlphaVantageAPI implements DataRequester
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

	@Override
	public double getPrice(String symbol) throws IOException
	{
		JSONObject json = getAsJSON(symbol, "GLOBAL_QUOTE");
		JSONObject quote = (JSONObject)json.get("Global Quote");
		return AlphaVantageAPI.getDouble(quote, "05. price");
	}

	public void requestData(SymbolData data) throws IOException
	{
		JSONObject json;

		json = getAsJSON(data.symbol, "OVERVIEW");
		data.name = getString(json, "Name");
		data.eps = getDouble(json, "EPS");
		data.analystTargetPrice = getDouble(json, "AnalystTargetPrice");

		json = getAsJSON(data.symbol, "INCOME_STATEMENT");
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

		json = getAsJSON(data.symbol, "BALANCE_SHEET");
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

		LocalDate yearsAgo = LocalDate.now().minusYears(6);

		json = getAsJSON(data.symbol, "EARNINGS");
		JSONArray annualEarnings = (JSONArray)json.get("annualEarnings");
		List<DateAndDouble> earnings = new ArrayList<>();
		for (Object r : annualEarnings)
		{
			JSONObject report = (JSONObject)r;
			LocalDate date = getDate(report, "fiscalDateEnding");
			double eps = getDouble(report, "reportedEPS");
			earnings.add(new DateAndDouble(date, eps));
		}
		earnings.stream().filter(e -> e.date.isAfter(yearsAgo)).sorted().forEach(data.earnings::add);

		json = getAsJSON(data.symbol, "TIME_SERIES_MONTHLY_ADJUSTED");
		JSONObject timeSeries = (JSONObject)json.get("Monthly Adjusted Time Series");
		List<LocalDate> dates = new ArrayList<>();
		for (Object key : timeSeries.keySet())
		{
			LocalDate date = LocalDate.parse((String)key);
			if (date.isAfter(yearsAgo))
				dates.add(date);
		}
		Collections.sort(dates);
		for (LocalDate date : dates)
		{
			JSONObject prices = (JSONObject)timeSeries.get(date.toString());
			double closePrice = getDouble(prices, "5. adjusted close");
			data.priceHistory.add(new DateAndDouble(date, closePrice));
		}
	}

	private JSONObject getAsJSON(String symbol, String function) throws IOException
	{
		String url = new StringBuilder(urlBase).append("&function=").append(function).append("&symbol=").append(symbol).toString();
		String cacheKey = symbol + "/" + function + ".json";
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

	private static LocalDate getDate(JSONObject obj, String key)
	{
		return LocalDate.parse((String)obj.get(key));
	}
}
