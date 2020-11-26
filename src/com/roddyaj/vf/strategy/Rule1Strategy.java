package com.roddyaj.vf.strategy;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.roddyaj.vf.model.DateAndDouble;
import com.roddyaj.vf.model.Result;
import com.roddyaj.vf.model.SymbolData;
import com.roddyaj.vf.model.SymbolData.BalanceSheet;
import com.roddyaj.vf.model.SymbolData.IncomeStatement;
import com.roddyaj.vf.model.SymbolResult;

public class Rule1Strategy implements Strategy
{
	@Override
	public boolean evaluate(SymbolData data, SymbolResult result) throws IOException
	{
		boolean pass = testROIC(data, result) && testMosPrice(data, result);
		result.addResult(new Result("Rule1", pass));
		return pass;
	}

	private boolean testROIC(SymbolData data, SymbolResult result) throws IOException
	{
		boolean pass = true;
		List<IncomeStatement> incomeStatements = data.getIncomeStatements();
		List<BalanceSheet> balanceSheets = data.getBalanceSheets();
		int periods = Math.min(incomeStatements.size(), balanceSheets.size());
		double[] roics = new double[periods];
		for (int i = 0; i < periods; i++)
		{
			IncomeStatement incomeStatement = incomeStatements.get(i);
			BalanceSheet balanceSheet = balanceSheets.get(i);
			double nopat = incomeStatement.operatingIncome * (1 - (double)incomeStatement.taxProvision / incomeStatement.incomeBeforeTax);
			long investedCapital = balanceSheet.shortTermDebt + balanceSheet.longTermDebt + balanceSheet.totalShareholderEquity;
			double roic = nopat / investedCapital;
			roics[i] = roic * 100;
//			System.out.println(data.symbol + " ROIC " + incomeStatement.period + " " + (roic * 100));
			pass &= roic >= .1;
		}
		String roicValues = Arrays.stream(roics).mapToObj(r -> String.format("%.1f", r)).collect(Collectors.joining(","));
		result.addResult(new Result("Rule1.ROIC " + roicValues, pass));
		return pass;
	}

	private boolean testMosPrice(SymbolData data, SymbolResult result) throws IOException
	{
		List<BalanceSheet> balanceSheets = data.getBalanceSheets();
		if (balanceSheets.size() < 2)
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
			int years = balanceSheets.size() - 1;
			double recentEquity = balanceSheets.get(0).totalShareholderEquity;
			double olderEquity = balanceSheets.get(balanceSheets.size() - 1).totalShareholderEquity;
			if (recentEquity > 0 && olderEquity > 0)
				pastGrowthRate = Math.pow(recentEquity / olderEquity, 1. / years);
			else
				pastGrowthRate = 0;
		}
		double analystGrowthRate = 100; // TODO
		double estimatedGrowthRate = Math.min(pastGrowthRate, analystGrowthRate);

		// Estimated future PE
		double defaultPE = 2 * (estimatedGrowthRate - 1) * 100;
		double historicalPE = calcHistoricalPE(data);
		double estimatedFuturePE = Math.min(defaultPE, historicalPE);

		// Work towards final price
		double futureEps = data.getEps() * Math.pow(estimatedGrowthRate, projectedYears);
		double futureMarketPrice = futureEps * estimatedFuturePE;
		double stickerPrice = futureMarketPrice / Math.pow(marr, projectedYears);
		double mosPrice = stickerPrice * mosFactor;

		boolean pass = data.getPrice() < mosPrice;
		result.addResult(new Result("Rule1.mosPrice", pass, mosPrice));
		return pass;
	}

	private double calcHistoricalPE(SymbolData data) throws IOException
	{
		double peSum = 0;
		int peCount = 0;

		List<DateAndDouble> earnings = data.getEarnings();
		List<DateAndDouble> priceHistory = data.getPriceHistory();

		LocalDate prevDate = null;
		for (DateAndDouble earningsElement : earnings)
		{
			double priceSum = 0;
			int priceCount = 0;
			LocalDate prevYear = prevDate != null ? prevDate : earningsElement.date.minusYears(1);
			prevDate = earningsElement.date;
			for (DateAndDouble priceElement : priceHistory)
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
