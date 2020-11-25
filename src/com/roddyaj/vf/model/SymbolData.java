package com.roddyaj.vf.model;

import java.io.IOException;
import java.util.List;

public class SymbolData
{
	public final String symbol;

	private String name;
	private double eps;
	private double analystTargetPrice;

	private List<IncomeStatement> incomeStatements;

	private List<BalanceSheet> balanceSheets;

	private List<DateAndDouble> earnings;

	private List<DateAndDouble> priceHistory;

	private double price;

	private DataRequester requester;

	public SymbolData(String symbol)
	{
		this.symbol = symbol;
	}

	public void setRequester(DataRequester requester)
	{
		this.requester = requester;
	}

	public String getName() throws IOException
	{
		if (name == null)
			name = requester.getName(symbol);
		return name;
	}

	public double getEps() throws IOException
	{
		if (eps == 0)
			eps = requester.getEps(symbol);
		return eps;
	}

	public double getAnalystTargetPrice() throws IOException
	{
		if (analystTargetPrice == 0)
			analystTargetPrice = requester.getAnalystTargetPrice(symbol);
		return analystTargetPrice;
	}

	public List<IncomeStatement> getIncomeStatements() throws IOException
	{
		if (incomeStatements == null)
			incomeStatements = requester.getIncomeStatements(symbol);
		return incomeStatements;
	}

	public List<BalanceSheet> getBalanceSheets() throws IOException
	{
		if (balanceSheets == null)
			balanceSheets = requester.getBalanceSheets(symbol);
		return balanceSheets;
	}

	public List<DateAndDouble> getEarnings() throws IOException
	{
		if (earnings == null)
			earnings = requester.getEarnings(symbol);
		return earnings;
	}

	public List<DateAndDouble> getPriceHistory() throws IOException
	{
		if (priceHistory == null)
			priceHistory = requester.getPriceHistory(symbol);
		return priceHistory;
	}

	public void setPrice(double price)
	{
		this.price = price;
	}

	public double getPrice() throws IOException
	{
		if (price == 0)
			price = requester.getPrice(symbol);
		return price;
	}

	public static class IncomeStatement
	{
		public String period;
		public long incomeBeforeTax;
		public long operatingIncome;
		public long taxProvision;
	}

	public static class BalanceSheet
	{
		public String period;
		public long totalShareholderEquity;
		public long shortTermDebt;
		public long longTermDebt;
	}

	public interface DataRequester
	{
		String getName(String symbol) throws IOException;

		double getEps(String symbol) throws IOException;

		double getAnalystTargetPrice(String symbol) throws IOException;

		List<IncomeStatement> getIncomeStatements(String symbol) throws IOException;

		List<BalanceSheet> getBalanceSheets(String symbol) throws IOException;

		List<DateAndDouble> getEarnings(String symbol) throws IOException;

		List<DateAndDouble> getPriceHistory(String symbol) throws IOException;

		double getPrice(String symbol) throws IOException;
	}
}
