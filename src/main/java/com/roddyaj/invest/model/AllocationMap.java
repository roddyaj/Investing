package com.roddyaj.invest.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.roddyaj.invest.model.Message.Level;
import com.roddyaj.invest.model.settings.Allocation;

public class AllocationMap
{
	private final Map<String, Double> allocationMap = new HashMap<>();

	public AllocationMap(Allocation[] allocations, double untrackedPercent, List<Message> messages)
	{
		if (allocations == null || allocations.length == 0)
		{
			messages.add(new Message(Level.WARN, "No allocations specified"));
			return;
		}

		// Create a map of category => percent from the config
		Map<String, Double> map = new HashMap<>();
		for (Allocation allocation : allocations)
			map.put(allocation.getCat(), allocation.getPercent() / 100);

		// Fill in the untracked percent
		String untrackedCategory = map.keySet().stream().filter(c -> getSymbol(c).equals("untracked")).findAny().orElse(null);
		if (untrackedCategory != null)
		{
			double factor = getTotalAllocation(getParent(untrackedCategory), map);
			double untrackedPctAdjusted = untrackedPercent / factor;
			map.put(untrackedCategory, untrackedPctAdjusted);
			System.out.println("Untracked percent: " + String.format("%.2f", untrackedPctAdjusted * 100));
			System.out.println("Tracked percent:   " + String.format("%.2f", (1 - untrackedPctAdjusted) * 100));
		}

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
				messages.add(new Message(Level.WARN, "Category '" + parent + "' doesn't add up to 100%: " + (childSum * 100)));
		}

		// Create the allocation map with final percentages
		for (String category : map.keySet())
		{
			boolean isLeaf = getChildren(category, map).count() == 0;
			if (isLeaf)
			{
				String symbol = getSymbol(category);
				double allocation = getTotalAllocation(category, map);

				if (allocationMap.containsKey(symbol))
					allocation += allocationMap.get(symbol).doubleValue();
				allocationMap.put(symbol, allocation);
			}
		}

		// Validate total percent
		double totalPercent = allocationMap.values().stream().mapToDouble(Double::doubleValue).sum();
		if (Math.abs(1 - totalPercent) > 0.00001)
			messages.add(new Message(Level.WARN, "Total doesn't add up to 100%: " + (totalPercent * 100)));

//		for (Map.Entry<String, Double> entry : allocationMap.entrySet())
//			System.out.println(entry.getKey() + " " + entry.getValue() * 100);
	}

	public double getAllocation(String symbol)
	{
		return allocationMap.getOrDefault(symbol, 0.);
	}

	private static double getTotalAllocation(String category, Map<String, Double> map)
	{
		double allocation = 1;
		String[] tokens = category.split("\\.");
		for (int i = tokens.length; i > 0; i--)
		{
			String partialKey = String.join(".", Arrays.copyOfRange(tokens, 0, i));
			if (map.containsKey(partialKey))
				allocation *= map.get(partialKey).doubleValue();
		}
		return allocation;
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
