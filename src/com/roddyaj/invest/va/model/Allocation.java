package com.roddyaj.invest.va.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Allocation
{
	private final Map<String, Double> allocationMap = new HashMap<>();

	public Allocation(com.roddyaj.invest.va.model.config.Allocation[] allocations)
	{
		Map<String, Double> map = new HashMap<>();
		for (var allocation : allocations)
			map.put(allocation.getCat(), allocation.getPercent() / 100);

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
				allocationMap.put(symbol, allocation);
			}
		}

//		for (Map.Entry<String, Double> entry : allocationMap.entrySet())
//			System.out.println(entry.getKey() + " " + entry.getValue());
	}

	public double getAllocation(String symbol)
	{
		return allocationMap.getOrDefault(symbol, 0.);
	}
}
