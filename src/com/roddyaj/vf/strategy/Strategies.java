package com.roddyaj.vf.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.roddyaj.vf.model.Results;
import com.roddyaj.vf.model.SymbolData;
import com.roddyaj.vf.model.SymbolResult;

public class Strategies
{
	private final List<Strategy> strategies = new ArrayList<>();

	public Strategies(String[] args)
	{
		String strategiesArg = null;
		for (String arg : args)
		{
			if (arg.startsWith("strat="))
				strategiesArg = arg.split("=")[1];
		}
		if (strategiesArg == null)
			strategiesArg = "AnalystTarget,Rule1";

		List<Strategy> availableStrategies = new ArrayList<>();
		availableStrategies.add(new AnalystTargetStrategy());
		availableStrategies.add(new Rule1Strategy());

		Map<String, Strategy> map = new HashMap<>();
		for (Strategy strategy : availableStrategies)
			map.put(strategy.getName(), strategy);

		String[] strategyNames = strategiesArg.split(",");
		for (String name : strategyNames)
		{
			Strategy strategy = map.get(name);
			if (strategy != null)
				strategies.add(strategy);
		}
	}

	public void run(Collection<? extends SymbolData> stocks) throws IOException
	{
		StringBuilder preamble = new StringBuilder();
		for (Strategy strategy : strategies)
		{
			if (preamble.length() > 0)
				preamble.append(", ");
			preamble.append(strategy.getName());
		}
		preamble.insert(0, "\nRunning strategies: ");
		System.out.println(preamble);

		Results results = new Results();
		for (SymbolData stock : stocks)
			run(stock, results);
		System.out.println(results);
	}

	private void run(SymbolData stock, Results results) throws IOException
	{
		boolean allPass = true;
		SymbolResult result = new SymbolResult(stock);
		for (Strategy strategy : strategies)
		{
			allPass &= strategy.evaluate(stock, result);
			if (!allPass)
				break;
		}
		result.pass = allPass;

		results.addResult(result);
	}
}
