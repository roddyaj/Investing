package com.roddyaj.invest.api.finnhub;

import java.io.IOException;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roddyaj.invest.api.model.Quote;
import com.roddyaj.invest.api.model.QuoteProvider;
import com.roddyaj.invest.network.HttpClientNew;
import com.roddyaj.invest.network.Response;

public class FinnhubAPI implements QuoteProvider
{
	private static final String urlRoot = "https://finnhub.io/api/v1/";

	private String apiKey;

	private int requestLimitPerMinute;

	@Override
	public String getName()
	{
		return "Finnhub";
	}

	@Override
	public Quote getQuote(String symbol) throws IOException
	{
		JsonNode json = request(symbol, "quote");
		double price = getDouble(json, "c");
		double changePercent = getDouble(json, "dp");
		return new Quote(price, changePercent);
	}

	public void setApiKey(String apiKey)
	{
		this.apiKey = apiKey;
	}

	public void setRequestLimitPerMinute(int requestLimitPerMinute)
	{
		this.requestLimitPerMinute = requestLimitPerMinute;
	}

	private JsonNode request(String symbol, String function) throws IOException
	{
		String url = new StringBuilder(urlRoot).append(function).append("?symbol=").append(symbol).append("&token=").append(apiKey).toString();
		Response response = HttpClientNew.SHARED_INSTANCE.get(url, requestLimitPerMinute, Duration.ofMinutes(10));
		return new ObjectMapper().readTree(response.getBody());
	}

	private static double getDouble(JsonNode obj, String key)
	{
		return obj.get(key).doubleValue();
	}
}
