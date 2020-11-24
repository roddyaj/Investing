package com.roddyaj.vf.strategy;

import com.roddyaj.vf.model.SymbolData;

public class AnalystTargetStrategy implements Strategy
{
	@Override
	public boolean evaluate(SymbolData data)
	{
		return data.price < (data.analystTargetPrice * 0.95);
	}
}
