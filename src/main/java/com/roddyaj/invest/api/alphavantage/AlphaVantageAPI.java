package com.roddyaj.invest.api.alphavantage;

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
