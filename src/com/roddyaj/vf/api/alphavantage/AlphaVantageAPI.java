package com.roddyaj.vf.api.alphavantage;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.roddyaj.vf.model.DateAndDouble;
import com.roddyaj.vf.model.SymbolData.BalanceSheet;
import com.roddyaj.vf.model.SymbolData.DataRequester;
import com.roddyaj.vf.model.SymbolData.IncomeStatement;
import com.roddyaj.vf.request.RequestCache;

public class AlphaVantageAPI implements DataRequester
{
	private static final String urlRoot = "https://www.alphavantage.co/query?";

	private final String urlBase;

	private final Map<String, JSONObject> cache = new HashMap<>();

	private final RequestCache requestor;

	private String lastSymbol;

	public AlphaVantageAPI(JSONObject settings) throws IOException
	{
		String apiKey = (String)settings.get("alphavantage.apiKey");
		long sleepTime = Duration.parse((String)settings.get("alphavantage.sleep")).toMillis();
		if (apiKey == null)
			throw new IOException("Error: No API key specified");

		urlBase = new StringBuilder(urlRoot).append("apikey=").append(apiKey).toString();
		requestor = new RequestCache(sleepTime, settings);
	}

	@Override
	public String getName(String symbol) throws IOException
	{
		JSONObject json = request(symbol, "OVERVIEW");
		return getString(json, "Name");
	}

	@Override
	public double getEps(String symbol) throws IOException
	{
		JSONObject json = request(symbol, "OVERVIEW");
		return getDouble(json, "EPS");
	}

	@Override
	public double getAnalystTargetPrice(String symbol) throws IOException
	{
		JSONObject json = request(symbol, "OVERVIEW");
		return getDouble(json, "AnalystTargetPrice");
	}

	@Override
	public List<IncomeStatement> getIncomeStatements(String symbol) throws IOException
	{
		List<IncomeStatement> incomeStatements = new ArrayList<>();

		JSONObject json = request(symbol, "INCOME_STATEMENT");
		JSONArray annualReports = (JSONArray)json.get("annualReports");
		for (Object r : annualReports)
		{
			JSONObject report = (JSONObject)r;
			IncomeStatement incomeStatement = new IncomeStatement();
			incomeStatement.period = getString(report, "fiscalDateEnding");
			incomeStatement.incomeBeforeTax = getDouble(report, "incomeBeforeTax");
			incomeStatement.operatingIncome = getDouble(report, "operatingIncome");
			incomeStatement.taxProvision = getDouble(report, "taxProvision");
			incomeStatements.add(incomeStatement);
		}

		return incomeStatements;
	}

	@Override
	public List<BalanceSheet> getBalanceSheets(String symbol) throws IOException
	{
		List<BalanceSheet> balanceSheets = new ArrayList<>();

		JSONObject json = request(symbol, "BALANCE_SHEET");
		JSONArray annualReports = (JSONArray)json.get("annualReports");
		for (Object r : annualReports)
		{
			JSONObject report = (JSONObject)r;
			BalanceSheet balanceSheet = new BalanceSheet();
			balanceSheet.period = getString(report, "fiscalDateEnding");
			balanceSheet.totalShareholderEquity = getDouble(report, "totalShareholderEquity");
			balanceSheet.shortTermDebt = getDouble(report, "shortTermDebt");
			balanceSheet.longTermDebt = getDouble(report, "longTermDebt");
			balanceSheets.add(balanceSheet);
		}

		return balanceSheets;
	}

	@Override
	public List<DateAndDouble> getEarnings(String symbol) throws IOException
	{
		List<DateAndDouble> earnings = new ArrayList<>();

		LocalDate yearsAgo = LocalDate.now().minusYears(6);

		JSONObject json = request(symbol, "EARNINGS");
		JSONArray annualEarnings = (JSONArray)json.get("annualEarnings");
		for (Object r : annualEarnings)
		{
			JSONObject report = (JSONObject)r;
			LocalDate date = getDate(report, "fiscalDateEnding");
			double eps = getDouble(report, "reportedEPS");
			if (date.isAfter(yearsAgo))
				earnings.add(new DateAndDouble(date, eps));
		}
		Collections.sort(earnings);

		return earnings;
	}

	@Override
	public List<DateAndDouble> getPriceHistory(String symbol) throws IOException
	{
		List<DateAndDouble> priceHistory = new ArrayList<>();

		LocalDate yearsAgo = LocalDate.now().minusYears(6);

		JSONObject json = request(symbol, "TIME_SERIES_MONTHLY_ADJUSTED");
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
			priceHistory.add(new DateAndDouble(date, closePrice));
		}

		return priceHistory;
	}

	@Override
	public double getPrice(String symbol) throws IOException
	{
		JSONObject json = request(symbol, "GLOBAL_QUOTE");
		JSONObject quote = (JSONObject)json.get("Global Quote");
		return AlphaVantageAPI.getDouble(quote, "05. price");
	}

	private JSONObject request(String symbol, String function) throws IOException
	{
		JSONObject response;

		if (!symbol.equals(lastSymbol))
		{
			cache.clear();
			lastSymbol = symbol;
		}

		final String cacheKey = symbol + "/" + function + ".json";
		response = cache.get(cacheKey);
		if (response == null)
		{
			String url = new StringBuilder(urlBase).append("&function=").append(function).append("&symbol=").append(symbol).toString();
			response = requestor.getJson(URI.create(url), cacheKey);
			cache.put(cacheKey, response);
		}

		return response;
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
		String value = (String)obj.get(key);
		return "None".equals(value) ? 0 : Double.parseDouble(value);
	}

	private static LocalDate getDate(JSONObject obj, String key)
	{
		return LocalDate.parse((String)obj.get(key));
	}
}
