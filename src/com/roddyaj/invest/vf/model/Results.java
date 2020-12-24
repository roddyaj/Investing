package com.roddyaj.invest.vf.model;

import java.util.ArrayList;
import java.util.List;

public class Results
{
	public final List<SymbolResult> passes = new ArrayList<>();

	public final List<SymbolResult> fails = new ArrayList<>();

	public void addResult(SymbolResult result)
	{
		if (result.pass)
			passes.add(result);
		else
			fails.add(result);
	}
}
