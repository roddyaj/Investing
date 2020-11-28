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
		result.addResult(new Result("AnalystTarget", pass, targetPrice));
		return pass;
	}
}
