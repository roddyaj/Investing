package com.roddyaj.invest.network;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * HTTP client. Create one and reuse it.<br>
 * Features:<br>
 * - Caching (memory and disk)<br>
 * - Request throttling - Uses Square's OkHttp library for requests<br>
 */
public class HttpClientNew
{
	public static final HttpClientNew SHARED_INSTANCE = new HttpClientNew();

	// OkHttp
	private final OkHttpClient client;
	private static final CacheControl noCacheControl = new CacheControl.Builder().noCache().noStore().build();

	// Memory caching
	private final com.google.common.cache.Cache<String, Response> memoryCache = com.google.common.cache.CacheBuilder.newBuilder().maximumSize(1000)
			.build();

	// Throttling
	private final Map<String, Long> lastRequestMap = new ConcurrentHashMap<>();
	private static final long MIN_THROTTLE_SLEEP_MILLIS = 100;

	public HttpClientNew()
	{
		Path cachePath = Paths.get(System.getProperty("user.home"), ".invest", "http-cache");
		Cache cache = new Cache(cachePath.toFile(), 10 * 1024 * 1024);
		client = new OkHttpClient.Builder().cache(cache).build();
	}

	public Response get(String url) throws IOException
	{
		return get(url, null, Duration.ofMinutes(60));
	}

	public Response get(String url, Number requestLimitPerMinute, Duration maxStale) throws IOException
	{
		boolean useCache = maxStale != null;
		CacheControl cacheControl = useCache ? new CacheControl.Builder().maxStale((int)maxStale.getSeconds(), TimeUnit.SECONDS).build()
				: noCacheControl;
		Request request = new Request.Builder().cacheControl(cacheControl).url(url).build();
		return request(request, requestLimitPerMinute, useCache, url);
	}

	public void clearCache()
	{
		clearMemoryCache();
		System.out.println("Clearing HTTP disk cache");
		try
		{
			client.cache().evictAll();
		}
		catch (IOException e)
		{
			System.err.println(e);
		}
	}

	public void clearMemoryCache()
	{
		System.out.println("Clearing HTTP memory cache");
		memoryCache.invalidateAll();
		memoryCache.cleanUp();
	}

	public long getCacheSize()
	{
		long size = 0;
		try
		{
			size = client.cache().size();
		}
		catch (IOException e)
		{
			System.err.println(e);
		}
		return size;
	}

	private Response request(Request request, Number requestLimitPerMinute, boolean useCache, String cacheKey) throws IOException
	{
		long start = System.nanoTime();

		Response response;
//		if (useCache)
//		{
//			try
//			{
//				response = memoryCache.get(cacheKey, () -> requestOkHttp(request, requestLimitPerMinute, useCache));
//			}
//			catch (ExecutionException e)
//			{
//				throw new IOException(e);
//			}
//		}
//		else
//		{
		response = requestOkHttp(request, requestLimitPerMinute, useCache);
//		}

		System.out.println(getLogMessage(request.url().toString(), request.method(), start));

		return response;
	}

	private Response requestOkHttp(Request request, Number requestLimitPerMinute, boolean useCache) throws IOException
	{
		Response response;

		// Throttle if it's going to be a network request
		if (!useCache || client.cache().get$okhttp(request) == null)
		{
			throttle(request.url().toString(), requestLimitPerMinute);
		}

		try (okhttp3.Response okHttpResponse = client.newCall(request).execute())
		{
			response = new Response(okHttpResponse);
		}

		return response;
	}

	private void throttle(String url, Number requestLimitPerMinute)
	{
		String host = getHost(url);
		Long lastRequest = lastRequestMap.get(host);
		if (lastRequest != null)
		{
			long sleepTimeMillis = requestLimitPerMinute != null
					? Math.max(Math.round(60000 / requestLimitPerMinute.doubleValue()), MIN_THROTTLE_SLEEP_MILLIS)
					: MIN_THROTTLE_SLEEP_MILLIS;
			long timeToSleep = sleepTimeMillis - (System.currentTimeMillis() - lastRequest.longValue());
			if (timeToSleep > 0)
			{
				System.out.println("Wait " + timeToSleep + " ms for:     " + url);
				try
				{
					Thread.sleep(timeToSleep);
				}
				catch (InterruptedException e)
				{
					System.err.println(e);
				}
			}
		}
		lastRequestMap.put(host, System.currentTimeMillis());
	}

	private static String getHost(String url)
	{
		try
		{
			return new URL(url).getHost();
		}
		catch (MalformedURLException e)
		{
			System.err.println(e);
			return url;
		}
	}

	private static String getLogMessage(String url, String method, long startNanos)
	{
		long deltaMicros = (System.nanoTime() - startNanos) / 1000;
		return String.format("Took%9d us: %-4s %s", deltaMicros, method, url);
	}
}
