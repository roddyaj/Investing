package com.roddyaj.vf.strategy;

import static java.lang.String.format;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.roddyaj.vf.model.DateAndDouble;
import com.roddyaj.vf.model.SymbolData;
import com.roddyaj.vf.model.SymbolData.BalanceSheet;
import com.roddyaj.vf.model.SymbolData.IncomeStatement;
import com.roddyaj.vf.model.SymbolResult;

public class Rule1Strategy extends AndStrategy
{
	public Rule1Strategy()
	{
		strategies.add(new RoicIsHigh());
		strategies.add(new InlineStrategy("Rule1.#BalanceSheets", d -> d.getBalanceSheets().size() >= 5));
		strategies.add(new MosPrice());
	}

	@Override
	public String getName()
	{
		return "Rule1";
	}

	private static class RoicIsHigh implements Strategy
	{
		private final static double MIN_ROIC = .1;

		@Override
		public String getName()
		{
			return "Rule1.ROIC";
		}

		@Override
		public boolean evaluate(SymbolData data, SymbolResult result) throws IOException
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
				double nopat = incomeStatement.operatingIncome * (1 - incomeStatement.taxProvision / incomeStatement.incomeBeforeTax);
				double investedCapital = balanceSheet.shortTermDebt + balanceSheet.longTermDebt + balanceSheet.totalShareholderEquity;
				double roic = nopat / investedCapital;
				roics[i] = roic * 100;
				pass &= roic >= MIN_ROIC;
			}
			String roicValues = Arrays.stream(roics).mapToObj(r -> format("%.1f", r)).collect(Collectors.joining(", "));
			result.addResult(getName(), roicValues, pass);
			return pass;
		}
	}

	private static class MosPrice implements Strategy
	{
		// Minimum acceptable rate of return
		private final static double MARR = 1.15;
		private final static double MOS_FACTOR = 0.5;
		private final static int PROJECTED_YEARS = 5;
		private final static double MIN_GROWTH_RATE = 1.1;

		@Override
		public String getName()
		{
			return "Rule1.mosPrice";
		}

		@Override
		public boolean evaluate(SymbolData data, SymbolResult result) throws IOException
		{
			// Estimated EPS growth rate
			double equityGrowthRate = Math.min(getEquityGrowthRate(data, 2), getEquityGrowthRate(data, 4));
			double estimatedGrowthRate = equityGrowthRate;
//			double analystGrowthRate = 100; // TODO
//			double estimatedGrowthRate = Math.min(equityGrowthRate, analystGrowthRate);

			// Estimated future PE
			double defaultPE = 2 * (estimatedGrowthRate - 1) * 100;
			double historicalPE = calcHistoricalPE(data);
			double estimatedFuturePE = Math.min(defaultPE, historicalPE);

			// Work towards final price
			double futureEps = data.getEps() * Math.pow(estimatedGrowthRate, PROJECTED_YEARS);
			double futureMarketPrice = futureEps * estimatedFuturePE;
			double stickerPrice = futureMarketPrice / Math.pow(MARR, PROJECTED_YEARS);
			double mosPrice = stickerPrice * MOS_FACTOR;

			boolean pass = estimatedGrowthRate >= MIN_GROWTH_RATE && data.getPrice() < mosPrice;

			String name = getName();
			result.addResult(name + ".eps", format("%.2f", data.getEps()));
			result.addResult(name + ".growthRate", format("%.1f", (estimatedGrowthRate - 1) * 100), estimatedGrowthRate >= MIN_GROWTH_RATE);
			result.addResult(name + ".historicalPE", format("%.1f", historicalPE));
			result.addResult(name + ".priceToSticker", format("%.1f%%", 100 * data.getPrice() / stickerPrice));
			result.addResult(name, format("%.2f", mosPrice), data.getPrice() < mosPrice);

			double targetPrice = data.getAnalystTargetPrice();
			result.sortValue = Math.max(mosPrice, targetPrice) / Math.min(mosPrice, targetPrice);
			result.addResult("Rule1.sortValue", format("%.2f", result.sortValue));

			return pass;
		}

		private double getEquityGrowthRate(SymbolData data, int years) throws IOException
		{
			double rate = 0;
			List<BalanceSheet> balanceSheets = data.getBalanceSheets();
			double recentEquity = balanceSheets.get(0).totalShareholderEquity;
			double olderEquity = balanceSheets.get(years).totalShareholderEquity;
			if (recentEquity > 0 && olderEquity > 0)
				rate = Math.pow(recentEquity / olderEquity, 1. / years);
			return rate;
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
}
