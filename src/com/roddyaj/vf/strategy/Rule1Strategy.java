package com.roddyaj.vf.strategy;

import com.roddyaj.vf.model.Pair;
import com.roddyaj.vf.model.SymbolData;

public class Rule1Strategy implements Strategy
{
	@Override
	public Pair<Boolean, String> evaluate(SymbolData data)
	{
		double mosPrice = calcMosPrice(data);

		boolean pass = data.price < mosPrice;
		String message = String.format("Rule 1: %7.2f", mosPrice);
		return new Pair<>(pass, message);
	}

	private double calcMosPrice(SymbolData data)
	{
		final double marr = 1.15; // Minimum acceptable rate of return
		final double mosFactor = 0.5;
		final int projectedYears = 5;

		// Estimated eps growth rate
		double pastGrowthRate;
		{
			int years = data.shareholderEquity.size() - 1;
			double recentEquity = data.shareholderEquity.get(0).second.doubleValue();
			double olderEquity = data.shareholderEquity.get(data.shareholderEquity.size() - 1).second.doubleValue();
			pastGrowthRate = Math.pow(recentEquity / olderEquity, 1. / years);
		}
		double analystGrowthRate = 100; // TODO
		double estimatedGrowthRate = Math.min(pastGrowthRate, analystGrowthRate);

		// Estimated future PE
		double defaultPE = 2 * (estimatedGrowthRate - 1) * 100;
		double historicalPE = 1000; // TODO
		double estimatedFuturePE = Math.min(defaultPE, historicalPE);

		// Work towards final price
		double futureEps = data.eps * Math.pow(estimatedGrowthRate, projectedYears);
		double futureMarketPrice = futureEps * estimatedFuturePE;
		double stickerPrice = futureMarketPrice / Math.pow(marr, projectedYears);
		double mosPrice = stickerPrice * mosFactor;

		return mosPrice;
	}
}
