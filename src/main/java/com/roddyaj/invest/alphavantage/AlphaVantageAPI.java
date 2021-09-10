package com.roddyaj.invest.alphavantage;

import java.io.IOException;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roddyaj.invest.network.HttpClientNew;
import com.roddyaj.invest.network.Response;

public class AlphaVantageAPI
{
	private static final String urlRoot = "https://www.alphavantage.co/query?";

	private final String urlBase;

	private final int requestLimitPerMinute;

	public AlphaVantageAPI(String apiKey, int requestLimitPerMinute)
	{
		urlBase = new StringBuilder(urlRoot).append("apikey=").append(apiKey).toString();
		this.requestLimitPerMinute = requestLimitPerMinute;
	}

//	@Override
//	public String getName(String symbol) throws IOException
//	{
//		JSONObject json = request(symbol, "OVERVIEW");
//		return getString(json, "Name");
//	}
//
//	@Override
//	public double getEps(String symbol) throws IOException
//	{
//		List<DateAndDouble> earnings = getEarnings(symbol);
//		return earnings.get(earnings.size() - 1).value;
//	}
//
//	@Override
//	public double getAnalystTargetPrice(String symbol) throws IOException
//	{
//		JSONObject json = request(symbol, "OVERVIEW");
//		return getDouble(json, "AnalystTargetPrice");
//	}
//
//	@Override
//	public List<IncomeStatement> getIncomeStatements(String symbol) throws IOException
//	{
//		List<IncomeStatement> incomeStatements = new ArrayList<>();
//
//		JSONObject json = request(symbol, "INCOME_STATEMENT");
//		JSONArray annualReports = (JSONArray)json.get("annualReports");
//		for (Object r : annualReports)
//		{
//			JSONObject report = (JSONObject)r;
//			IncomeStatement incomeStatement = new IncomeStatement();
//			incomeStatement.period = getString(report, "fiscalDateEnding");
//			incomeStatement.incomeBeforeTax = getDouble(report, "incomeBeforeTax");
//			incomeStatement.operatingIncome = getDouble(report, "operatingIncome");
//			incomeStatement.taxProvision = getDouble(report, "incomeTaxExpense");
//			incomeStatements.add(incomeStatement);
//		}
//
//		return incomeStatements;
//	}
//
//	@Override
//	public List<BalanceSheet> getBalanceSheets(String symbol) throws IOException
//	{
//		List<BalanceSheet> balanceSheets = new ArrayList<>();
//
//		JSONObject json = request(symbol, "BALANCE_SHEET");
//		JSONArray annualReports = (JSONArray)json.get("annualReports");
//		for (Object r : annualReports)
//		{
//			JSONObject report = (JSONObject)r;
//			BalanceSheet balanceSheet = new BalanceSheet();
//			balanceSheet.period = getString(report, "fiscalDateEnding");
//			balanceSheet.totalShareholderEquity = getDouble(report, "totalShareholderEquity");
//			balanceSheet.shortTermDebt = getDouble(report, "shortTermDebt");
//			balanceSheet.longTermDebt = getDouble(report, "longTermDebt");
//			balanceSheets.add(balanceSheet);
//		}
//
//		return balanceSheets;
//	}
//
//	@Override
//	public List<DateAndDouble> getEarnings(String symbol) throws IOException
//	{
//		List<DateAndDouble> earnings = new ArrayList<>();
//
//		LocalDate yearsAgo = LocalDate.now().minusYears(6);
//
//		JSONObject json = request(symbol, "EARNINGS");
//		JSONArray annualEarnings = (JSONArray)json.get("annualEarnings");
//		for (Object r : annualEarnings)
//		{
//			JSONObject report = (JSONObject)r;
//			LocalDate date = getDate(report, "fiscalDateEnding");
//			double eps = getDouble(report, "reportedEPS");
//			if (date.isAfter(yearsAgo))
//				earnings.add(new DateAndDouble(date, eps));
//		}
//		Collections.sort(earnings);
//
//		return earnings;
//	}
//
//	@Override
//	public List<DateAndDouble> getPriceHistory(String symbol) throws IOException
//	{
//		List<DateAndDouble> priceHistory = new ArrayList<>();
//
//		LocalDate yearsAgo = LocalDate.now().minusYears(6);
//
//		JSONObject json = request(symbol, "TIME_SERIES_MONTHLY_ADJUSTED");
//		JSONObject timeSeries = (JSONObject)json.get("Monthly Adjusted Time Series");
//		List<LocalDate> dates = new ArrayList<>();
//		for (Object key : timeSeries.keySet())
//		{
//			LocalDate date = LocalDate.parse((String)key);
//			if (date.isAfter(yearsAgo))
//				dates.add(date);
//		}
//		Collections.sort(dates);
//		for (LocalDate date : dates)
//		{
//			JSONObject prices = (JSONObject)timeSeries.get(date.toString());
//			double closePrice = getDouble(prices, "5. adjusted close");
//			priceHistory.add(new DateAndDouble(date, closePrice));
//		}
//
//		return priceHistory;
//	}

	public double getPrice(String symbol) throws IOException
	{
		JsonNode json = request(symbol, "GLOBAL_QUOTE");
		JsonNode quote = json.get("Global Quote");
		return getDouble(quote, "05. price");
	}

	private JsonNode request(String symbol, String function) throws IOException
	{
		String url = new StringBuilder(urlBase).append("&function=").append(function).append("&symbol=").append(symbol).toString();
		Response response = HttpClientNew.SHARED_INSTANCE.get(url, requestLimitPerMinute, Duration.ofMinutes(10));
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(response.getBody());
		return node;
	}

//	private static String getString(JSONObject obj, String key)
//	{
//		return (String)obj.get(key);
//	}

	private static double getDouble(JsonNode obj, String key)
	{
		String value = obj.get(key).textValue();
		return "None".equals(value) ? 0 : Double.parseDouble(value);
	}

//	private static LocalDate getDate(JSONObject obj, String key)
//	{
//		return LocalDate.parse((String)obj.get(key));
//	}
}
