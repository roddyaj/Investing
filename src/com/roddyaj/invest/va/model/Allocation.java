package com.roddyaj.invest.va.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.roddyaj.invest.util.JSONUtils;

public class Allocation
{
	private final Map<String, Double> allocations = new HashMap<>();

	public Allocation(JSONArray config)
	{
		Map<String, Double> map = new HashMap<>();
		for (Object allocationObj : config)
		{
			JSONObject allocation = (JSONObject)allocationObj;
			map.put((String)allocation.get("cat"), JSONUtils.getPercent(allocation, "percent"));
		}

		for (Map.Entry<String, Double> entry : map.entrySet())
		{
			String category = entry.getKey();
			String[] tokens = category.split("\\.");
			String lastToken = tokens[tokens.length - 1];
			if (lastToken.toUpperCase().equals(lastToken))
			{
				String symbol = lastToken;
				double allocation = 1;
				for (int i = tokens.length; i > 0; i--)
				{
					String partialKey = String.join(".", Arrays.copyOfRange(tokens, 0, i));
					allocation *= map.get(partialKey);
				}
				allocations.put(symbol, allocation);
			}
		}

//		for (Map.Entry<String, Double> entry : allocations.entrySet())
//			System.out.println(entry.getKey() + " " + entry.getValue());
	}

	public double getAllocation(String symbol)
	{
		return allocations.getOrDefault(symbol, 0.);
	}
}
