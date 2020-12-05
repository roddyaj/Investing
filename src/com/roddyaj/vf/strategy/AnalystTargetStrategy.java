package com.roddyaj.vf.strategy;

import static java.lang.String.format;

import java.io.IOException;

import com.roddyaj.vf.model.SymbolData;
import com.roddyaj.vf.model.SymbolResult;

public class AnalystTargetStrategy implements Strategy
{
	@Override
	public String getName()
	{
		return "AnalystTarget";
	}

	@Override
	public boolean evaluate(SymbolData data, SymbolResult result) throws IOException
	{
		double buyPrice = data.getAnalystTargetPrice() * 0.95;
		boolean pass = data.getPrice() < buyPrice;
		String name = getName();
		result.addResult(name + ".buyPrice", format("%.2f", buyPrice), pass);
		result.addResult(name + ".priceToTarget", format("%.1f%%", 100 * data.getPrice() / data.getAnalystTargetPrice()));
		return pass;
	}
}
