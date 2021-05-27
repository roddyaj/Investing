package com.roddyaj.invest.va.model.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AllocationMap
{
	private final Map<String, Double> allocationMap = new HashMap<>();

	public AllocationMap(Allocation[] allocations)
	{
		// Create a map of category => percent from the config
		Map<String, Double> map = new HashMap<>();
		for (Allocation allocation : allocations)
			map.put(allocation.getCat(), allocation.getPercent() / 100);

		// Calculate any relative values
		for (Map.Entry<String, Double> entry : map.entrySet())
		{
			String category = entry.getKey();
			double percent = entry.getValue();
			if (percent < 0)
			{
				double siblingPercent = map.entrySet().stream().filter(e -> isSibling(e.getKey(), category))
						.mapToDouble(e -> e.getValue().doubleValue()).filter(p -> p > 0).sum();
				double myPercent = 1 - siblingPercent;
				map.put(category, myPercent);
			}
		}

		// Validate percentages
		Set<String> parents = map.keySet().stream().map(AllocationMap::getParent).collect(Collectors.toSet());
		for (String parent : parents)
		{
			double childSum = getChildren(parent, map).mapToDouble(e -> e.getValue().doubleValue()).sum();
			if (Math.abs(1 - childSum) > 0.00001)
				System.out.println("\033[33mCategory '" + parent + "' doesn't add up to 100%: " + (childSum * 100) + "\033[0m");
		}

		// Create the allocation map with final percentages
		for (String category : map.keySet())
		{
			boolean isLeaf = getChildren(category, map).count() == 0;
			if (isLeaf)
			{
				double allocation = 1;
				String[] tokens = category.split("\\.");
				for (int i = tokens.length; i > 0; i--)
				{
					String partialKey = String.join(".", Arrays.copyOfRange(tokens, 0, i));
					if (map.containsKey(partialKey))
						allocation *= map.get(partialKey).doubleValue();
				}

				String symbol = getSymbol(category);
				if (allocationMap.containsKey(symbol))
					allocation += allocationMap.get(symbol).doubleValue();
				allocationMap.put(symbol, allocation);
			}
		}

		// Validate total percent
		double totalPercent = allocationMap.values().stream().mapToDouble(Double::doubleValue).sum();
		if (Math.abs(1 - totalPercent) > 0.00001)
			System.out.println("\033[33mTotal doesn't add up to 100%: " + (totalPercent * 100) + "\033[0m");

//		for (Map.Entry<String, Double> entry : allocationMap.entrySet())
//			System.out.println(entry.getKey() + " " + entry.getValue() * 100);
	}

	public double getAllocation(String symbol)
	{
		return allocationMap.getOrDefault(symbol, 0.);
	}

	private static Stream<Map.Entry<String, Double>> getChildren(String parent, Map<String, Double> map)
	{
		return map.entrySet().stream().filter(e -> getParent(e.getKey()).equals(parent));
	}

	private static boolean isSibling(String category1, String category2)
	{
		return !category1.equals(category2) && getParent(category1).equals(getParent(category2));
	}

	private static String getParent(String category)
	{
		int i = category.lastIndexOf('.');
		return i == -1 ? "" : category.substring(0, i);
	}

	private static String getSymbol(String category)
	{
		int i = category.lastIndexOf('.');
		return i == -1 ? category : category.substring(i + 1, category.length());
	}
}
