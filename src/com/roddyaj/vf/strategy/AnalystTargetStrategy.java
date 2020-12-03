package com.roddyaj.vf.strategy;

import java.io.IOException;

import com.roddyaj.vf.model.Result;
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
		double targetPrice = data.getAnalystTargetPrice() * 0.95;
		boolean pass = data.getPrice() < targetPrice;
		String priceRatio = String.format("%.1f%%", 100 * data.getPrice() / data.getAnalystTargetPrice());
		result.addResult(new Result("AnalystTarget " + priceRatio, pass, targetPrice));
		return pass;
	}
}
