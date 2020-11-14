package com.roddyaj.vf.api.alphavantage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class AlphaVantageAPI
{
	private static final String urlRoot = "https://www.alphavantage.co/query?";

	private final String urlBase;

	private final HttpClient client;

	private final HttpRequest.Builder requestBuilder;

	public AlphaVantageAPI(String apiKey)
	{
		urlBase = new StringBuilder(urlRoot).append("apikey=").append(apiKey).append('&').toString();
		client = HttpClient.newHttpClient();
		requestBuilder = HttpRequest.newBuilder();
	}

	public JSONObject getOverview(String symbol) throws IOException, InterruptedException, ParseException
	{
		String url = new StringBuilder(urlBase).append("function=OVERVIEW&symbol=").append(symbol).toString();
		HttpRequest request = requestBuilder.uri(URI.create(url)).build();
		HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

		System.out.println(url + ": " + response.statusCode());

		JSONParser parser = new JSONParser();
		return (JSONObject)parser.parse(response.body());
	}
}
