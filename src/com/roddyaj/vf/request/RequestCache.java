package com.roddyaj.vf.request;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class RequestCache
{
	private final HttpRequest.Builder requestBuilder;

	private final HttpClient client;

	private final Path cacheRoot;

	private final long sleepTime;

	private long lastRequestTime;

	public RequestCache(long sleepTime)
	{
		this.sleepTime = sleepTime;
		client = HttpClient.newHttpClient();
		requestBuilder = HttpRequest.newBuilder();
		cacheRoot = Paths.get(System.getProperty("user.home"), ".vf", "cache");
		if (!Files.exists(cacheRoot))
		{
			try
			{
				Files.createDirectories(cacheRoot);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public JSONObject getJson(URI uri, String cacheKey) throws IOException
	{
		String responseBody = getString(uri, cacheKey);
		JSONParser parser = new JSONParser();
		try
		{
			return (JSONObject)parser.parse(responseBody);
		}
		catch (ParseException e)
		{
			throw new IOException(e);
		}
	}

	public String getString(URI uri, String cacheKey) throws IOException
	{
		return get(uri, cacheKey, this::requestString, Files::readString);
	}

	private String requestString(HttpRequest request, Path cacheFile) throws IOException
	{
		try
		{
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
			Files.writeString(cacheFile, response.body());
			return response.body();
		}
		catch (InterruptedException e)
		{
			throw new IOException(e);
		}
	}

	private <T> T get(URI uri, String cacheKey, Requester<T> requester, Reader<T> reader) throws IOException
	{
		Path requestDir = Paths.get(cacheRoot.toString(), cacheKey);
		if (!Files.exists(requestDir))
			Files.createDirectory(requestDir);
//		Path propertiesFile = Paths.get(requestDir.toString(), "properties");
		Path responseFile = Paths.get(requestDir.toString(), "response");
		if (Files.exists(responseFile))
		{
			T cachedBody = reader.read(responseFile);
			return cachedBody;
		}
		else
		{
			long timeToSleep = sleepTime - (System.currentTimeMillis() - lastRequestTime);
			if (timeToSleep > 0)
			{
				try
				{
					Thread.sleep(timeToSleep);
				}
				catch (InterruptedException e)
				{
					throw new IOException(e);
				}
			}

			System.out.println("Remote: " + uri);
			HttpRequest request = requestBuilder.uri(uri).build();
			lastRequestTime = System.currentTimeMillis();
			return requester.request(request, responseFile);
		}
	}

	@FunctionalInterface
	private interface Reader<T>
	{
		T read(Path cacheFile) throws IOException;
	}

	@FunctionalInterface
	private interface Requester<T>
	{
		T request(HttpRequest request, Path cacheFile) throws IOException;
	}
}
