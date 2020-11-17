package com.roddyaj.vf.strategy;

import com.roddyaj.vf.model.Pair;
import com.roddyaj.vf.model.SymbolData;

public class AnalystTargetStrategy implements Strategy
{
	@Override
	public Pair<Boolean, String> evaluate(SymbolData data)
	{
		boolean pass = data.price < (data.analystTargetPrice * 0.85);
		String message = String.format("Analyst: %7.2f", data.analystTargetPrice);
		return new Pair<>(pass, message);
	}
}
