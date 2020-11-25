package com.roddyaj.vf.strategy;

import java.time.LocalDate;

import com.roddyaj.vf.model.DateAndDouble;
import com.roddyaj.vf.model.Report;
import com.roddyaj.vf.model.SymbolData;
import com.roddyaj.vf.model.SymbolData.BalanceSheet;
import com.roddyaj.vf.model.SymbolData.IncomeStatement;

public class Rule1Strategy implements Strategy
{
	@Override
	public boolean evaluate(SymbolData data, Report report)
	{
		boolean pass = testROIC(data) & testMosPrice(data);
		String message = (pass ? "pass" : "fail");
		report.addMessage("Rule 1", message);
		return pass;
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
		if (data.balanceSheets.size() < 2)
		{
			System.out.println("Skipping " + data.symbol + ", missing balance sheets");
			return false;
		}

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
		double historicalPE = calcHistoricalPE(data);
		double estimatedFuturePE = Math.min(defaultPE, historicalPE);

		// Work towards final price
		double futureEps = data.eps * Math.pow(estimatedGrowthRate, projectedYears);
		double futureMarketPrice = futureEps * estimatedFuturePE;
		double stickerPrice = futureMarketPrice / Math.pow(marr, projectedYears);
		double mosPrice = stickerPrice * mosFactor;

		boolean pass = data.price < mosPrice;
		return pass;
	}

	private double calcHistoricalPE(SymbolData data)
	{
		double peSum = 0;
		int peCount = 0;

		LocalDate prevDate = null;
		for (DateAndDouble earningsElement : data.earnings)
		{
			double priceSum = 0;
			int priceCount = 0;
			LocalDate prevYear = prevDate != null ? prevDate : earningsElement.date.minusYears(1);
			prevDate = earningsElement.date;
			for (DateAndDouble priceElement : data.priceHistory)
			{
				if (!priceElement.date.isAfter(earningsElement.date) && priceElement.date.isAfter(prevYear))
				{
					priceSum += priceElement.value;
					++priceCount;
				}
			}

			if (priceCount == 12 || earningsElement.date.getYear() == LocalDate.now().getYear())
			{
				double averagePrice = priceSum / priceCount;
				double adjustedEPS = earningsElement.value * (12. / priceCount);
				double pe = averagePrice / adjustedEPS;
				peSum += pe;
				++peCount;
			}
		}

		double averagePE = peSum / peCount;
		return averagePE;
	}
}
