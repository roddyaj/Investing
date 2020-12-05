package com.roddyaj.vf.model;

import java.util.ArrayList;
import java.util.List;

public class SymbolResult
{
	public final SymbolData data;

	public boolean pass;

	public final List<Result> results = new ArrayList<>();

	public double sortValue;

	public SymbolResult(SymbolData data)
	{
		this.data = data;
	}

	public void addResult(Result result)
	{
		results.add(result);
	}

	public void addResult(String property, Object value)
	{
		results.add(new Result(property, value));
	}

	public void addResult(String property, Object value, boolean pass)
	{
		results.add(new Result(property, value, pass));
	}

	public boolean hasResults()
	{
		return !results.isEmpty();
	}
}
