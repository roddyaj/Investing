package com.roddyaj.vf.strategy;

import java.io.IOException;

import com.roddyaj.vf.model.SymbolData;
import com.roddyaj.vf.model.SymbolResult;

public class InlineStrategy implements Strategy
{
	private final String name;
	private final Criterion criterion;

	public InlineStrategy(String name, Criterion criterion)
	{
		this.name = name;
		this.criterion = criterion;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public boolean evaluate(SymbolData data, SymbolResult result) throws IOException
	{
		boolean pass = criterion.test(data);
		result.addResult(name, null, pass);
		return pass;
	}

	public interface Criterion
	{
		boolean test(SymbolData data) throws IOException;
	}
}
