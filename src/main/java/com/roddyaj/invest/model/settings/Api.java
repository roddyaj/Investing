package com.roddyaj.invest.model.settings;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Api
{
	private String name;
	private String apiKey;
	private int requestsPerMinute;

	@JsonProperty("name")
	public String getName()
	{
		return name;
	}

	@JsonProperty("name")
	public void setName(String name)
	{
		this.name = name;
	}

	@JsonProperty("apiKey")
	public String getApiKey()
	{
		return apiKey;
	}

	@JsonProperty("apiKey")
	public void setApiKey(String apiKey)
	{
		this.apiKey = apiKey;
	}

	@JsonProperty("requestsPerMinute")
	public int getRequestsPerMinute()
	{
		return requestsPerMinute;
	}

	@JsonProperty("requestsPerMinute")
	public void setRequestsPerMinute(int requestsPerMinute)
	{
		this.requestsPerMinute = requestsPerMinute;
	}
}
