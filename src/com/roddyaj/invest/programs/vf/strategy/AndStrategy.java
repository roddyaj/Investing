package com.roddyaj.invest.programs.vf.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.roddyaj.invest.programs.vf.model.SymbolData;
import com.roddyaj.invest.programs.vf.model.SymbolResult;

public class AndStrategy implements Strategy
{
	protected final List<Strategy> strategies = new ArrayList<>();

	public void addStrategy(Strategy strategy)
	{
		strategies.add(strategy);
	}

	@Override
	public String getName()
	{
		return "And";
	}

	@Override
	public boolean evaluate(SymbolData data, SymbolResult result) throws IOException
	{
		boolean pass = true;
		for (Strategy strategy : strategies)
		{
			pass &= strategy.evaluate(data, result);
			if (!pass)
				break;
		}
		return pass;
	}
}
