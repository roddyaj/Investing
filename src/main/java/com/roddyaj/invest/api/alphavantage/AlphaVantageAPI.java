package com.roddyaj.invest.api.alphavantage;

import java.io.IOException;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roddyaj.invest.api.model.Quote;
import com.roddyaj.invest.api.model.QuoteProvider;
import com.roddyaj.invest.network.HttpClientNew;
import com.roddyaj.invest.network.Response;

public class AlphaVantageAPI implements QuoteProvider
{
	private static final String urlRoot = "https://www.alphavantage.co/query?";

	private String urlBase;

	private int requestLimitPerMinute;

	@Override
	public String getName()
	{
		return "AlphaVantage";
	}

	@Override
	public Quote getQuote(String symbol) throws IOException
	{
		JsonNode json = request(symbol, "GLOBAL_QUOTE");
		JsonNode quote = json.get("Global Quote");
		double price = getDouble(quote, "05. price");
		double changePercent = getPercent(quote, "10. change percent");
		return new Quote(price, changePercent);
	}

	public void setApiKey(String apiKey)
	{
		urlBase = new StringBuilder(urlRoot).append("apikey=").append(apiKey).toString();
	}

	public void setRequestLimitPerMinute(int requestLimitPerMinute)
	{
		this.requestLimitPerMinute = requestLimitPerMinute;
	}

	private JsonNode request(String symbol, String function) throws IOException
	{
		String url = new StringBuilder(urlBase).append("&function=").append(function).append("&symbol=").append(symbol).toString();
		Response response = HttpClientNew.SHARED_INSTANCE.get(url, requestLimitPerMinute, Duration.ofMinutes(10));
		return new ObjectMapper().readTree(response.getBody());
	}

	private static double getDouble(JsonNode obj, String key)
	{
		String value = obj.get(key).textValue();
		return "None".equals(value) ? 0 : Double.parseDouble(value);
	}

	private static double getPercent(JsonNode obj, String key)
	{
		String value = obj.get(key).textValue();
		return "None".equals(value) ? 0 : Double.parseDouble(value.replace("%", ""));
	}

//	private static String getString(JSONObject obj, String key)
//	{
//		return (String)obj.get(key);
//	}
//
//	private static LocalDate getDate(JSONObject obj, String key)
//	{
//		return LocalDate.parse((String)obj.get(key));
//	}
}
