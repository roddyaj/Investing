package com.roddyaj.invest.va.model.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AllocationMap
{
	private final Map<String, Double> allocationMap = new HashMap<>();

	public AllocationMap(Allocation[] allocations)
	{
		Map<String, Double> map = new HashMap<>();
		for (Allocation allocation : allocations)
			map.put(allocation.getCat(), allocation.getPercent() / 100);

		for (Map.Entry<String, Double> entry : map.entrySet())
		{
			String category = entry.getKey();
			String[] tokens = category.split("\\.");
			String lastToken = tokens[tokens.length - 1];
			if (lastToken.toUpperCase().equals(lastToken) || lastToken.equals("cash") || lastToken.equals("stocks"))
			{
				String symbol = lastToken;

				double allocation = 1;
				for (int i = tokens.length; i > 0; i--)
				{
					String partialKey = String.join(".", Arrays.copyOfRange(tokens, 0, i));
					if (map.containsKey(partialKey))
						allocation *= map.get(partialKey).doubleValue();
				}

				if (allocationMap.containsKey(symbol))
					allocation += allocationMap.get(symbol).doubleValue();
				allocationMap.put(symbol, allocation);
			}
		}

//		for (Map.Entry<String, Double> entry : allocationMap.entrySet())
//			System.out.println(entry.getKey() + " " + entry.getValue() * 100);
	}

	public double getAllocation(String symbol)
	{
		return allocationMap.getOrDefault(symbol, 0.);
	}
}
