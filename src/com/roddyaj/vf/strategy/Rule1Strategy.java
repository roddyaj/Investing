package com.roddyaj.vf.strategy;

import com.roddyaj.vf.model.Pair;
import com.roddyaj.vf.model.SymbolData;

public class Rule1Strategy implements Strategy
{
	@Override
	public Pair<Boolean, String> evaluate(SymbolData data)
	{
		double historicalPE = 1000; // TODO

		int years = data.shareholderEquity.size() - 1;
		double recentEquity = data.shareholderEquity.get(0).second.doubleValue();
		double olderEquity = data.shareholderEquity.get(data.shareholderEquity.size() - 1).second.doubleValue();
		double growthRate = Math.pow(recentEquity / olderEquity, 1. / years);
//		System.out.println(recentEquity + " " + olderEquity + " " + years + " " + growthRate);

		final double marr = 0.15;
		final double mosFactor = 0.5;

		double mosPrice = data.eps * Math.min(historicalPE, (growthRate - 1) * 100 * 2) * Math.pow(growthRate, 5) / Math.pow(1 + marr, 5) * mosFactor;

		boolean pass = data.price < mosPrice;
		String message = String.format("Rule 1: %7.2f", mosPrice);
		return new Pair<>(pass, message);
	}
}
