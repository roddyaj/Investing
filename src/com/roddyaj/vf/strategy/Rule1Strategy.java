package com.roddyaj.vf.strategy;

import com.roddyaj.vf.model.SymbolData;
import com.roddyaj.vf.model.SymbolData.BalanceSheet;
import com.roddyaj.vf.model.SymbolData.IncomeStatement;

public class Rule1Strategy implements Strategy
{
	@Override
	public boolean evaluate(SymbolData data)
	{
		return testROIC(data) & testMosPrice(data);
	}

	private boolean testROIC(SymbolData data)
	{
		boolean pass = true;
		int periods = Math.min(data.incomeStatements.size(), data.balanceSheets.size());
		for (int i = 0; i < periods; i++)
		{
			IncomeStatement incomeStatement = data.incomeStatements.get(i);
			BalanceSheet balanceSheet = data.balanceSheets.get(i);
			double nopat = incomeStatement.operatingIncome * (1 - (double)incomeStatement.taxProvision / incomeStatement.incomeBeforeTax);
			long investedCapital = balanceSheet.shortTermDebt + balanceSheet.longTermDebt + balanceSheet.totalShareholderEquity;
			double roic = nopat / investedCapital;
//			System.out.println(data.symbol + " ROIC " + incomeStatement.period + " " + (roic * 100));
			pass &= roic >= .1;
		}
		return pass;
	}

	private boolean testMosPrice(SymbolData data)
	{
		final double marr = 1.15; // Minimum acceptable rate of return
		final double mosFactor = 0.5;
		final int projectedYears = 5;

		// Estimated EPS growth rate
		double pastGrowthRate;
		{
			int years = data.balanceSheets.size() - 1;
			double recentEquity = data.balanceSheets.get(0).totalShareholderEquity;
			double olderEquity = data.balanceSheets.get(data.balanceSheets.size() - 1).totalShareholderEquity;
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

		boolean pass = data.price < mosPrice;
		return pass;
	}
}
