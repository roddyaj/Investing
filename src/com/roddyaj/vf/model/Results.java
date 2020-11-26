package com.roddyaj.vf.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Results
{
	private final List<SymbolResult> passes = new ArrayList<>();

	private final List<SymbolResult> fails = new ArrayList<>();

	public void addResult(SymbolResult result)
	{
		if (result.pass)
			passes.add(result);
		else
			fails.add(result);
	}

	@Override
	public String toString()
	{
		List<String> lines = new ArrayList<>();
		format(fails, "Fail", lines);
		format(passes, "Pass", lines);
		return String.join("\n", lines);
	}

	private void format(Collection<? extends SymbolResult> results, String title, Collection<? super String> lines)
	{
		if (!results.isEmpty())
		{
			lines.add("");
			lines.add("=================== " + title + " ===================");
			for (SymbolResult result : results)
			{
				lines.add("");
				lines.add(result.toString());
			}
		}
	}
}
