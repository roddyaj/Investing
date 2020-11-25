package com.roddyaj.vf.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SymbolData
{
	public final String symbol;

	public String name;
	public double eps;
	public double analystTargetPrice;

	public final List<IncomeStatement> incomeStatements = new ArrayList<>();

	public final List<BalanceSheet> balanceSheets = new ArrayList<>();

	public final List<DateAndDouble> earnings = new ArrayList<>();

	public final List<DateAndDouble> priceHistory = new ArrayList<>();

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

	public void setPrice(double price)
	{
		this.price = price;
	}

	public double getPrice(String symbol) throws IOException
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
		double getPrice(String symbol) throws IOException;
	}
}
