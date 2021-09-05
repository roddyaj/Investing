package com.roddyaj.invest.network;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class HttpClient
{
	private static final java.net.http.HttpClient CLIENT = java.net.http.HttpClient.newHttpClient();

	public static String get(String url) throws IOException
	{
		HttpRequest.Builder builder = HttpRequest.newBuilder();
		HttpRequest request = builder.uri(URI.create(url)).build();
		HttpResponse<String> response;
		try
		{
			response = CLIENT.send(request, BodyHandlers.ofString());
		}
		catch (InterruptedException e)
		{
			throw new IOException(e);
		}
		return response.body();
	}
}
