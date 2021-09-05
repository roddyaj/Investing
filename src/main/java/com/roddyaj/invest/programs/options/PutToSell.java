package com.roddyaj.invest.programs.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.roddyaj.invest.util.HtmlFormatter;

public class PutToSell implements Comparable<PutToSell>
{
	private final String symbol;
	private final double availableAmount;
	private final Double underlyingPrice;
	private final Double dayChangePct;
	private double averageReturn;
	private int openOrderQuantity;

	public PutToSell(String symbol, double availableAmount, Double underlyingPrice, Double dayChangePct)
	{
		this.symbol = symbol;
		this.availableAmount = availableAmount;
		this.underlyingPrice = underlyingPrice;
		this.dayChangePct = dayChangePct;
	}

	public void setAverageReturn(double averageReturn)
	{
		this.averageReturn = averageReturn;
	}

	public void setOpenOrderQuantity(int openOrderQuantity)
	{
		this.openOrderQuantity = openOrderQuantity;
	}

	public String getSymbol()
	{
		return symbol;
	}

	public double getAvailableAmount()
	{
		return availableAmount;
	}

	public Double getUnderlyingPrice()
	{
		return underlyingPrice;
	}

	public Double getDayChangePct()
	{
		return dayChangePct;
	}

	public double getAverageReturn()
	{
		return averageReturn;
	}

	public int getOpenOrderQuantity()
	{
		return openOrderQuantity;
	}

	@Override
	public int compareTo(PutToSell o)
	{
		int result = Double.compare(dayChangePct != null ? dayChangePct.doubleValue() : 0, o.dayChangePct != null ? o.dayChangePct.doubleValue() : 0);
		if (result == 0)
		{
			result = Double.compare(o.averageReturn, averageReturn);
			if (result == 0)
				result = symbol.compareTo(o.symbol);
		}
		return result;
	}

	public static List<String> toBlock(Collection<? extends PutToSell> puts, double availableToTrade)
	{
		String info = String.format("$%.0f available", availableToTrade);
		return new PutHtmlFormatter().toBlock(puts, "Candidate Puts To Sell", info);
	}

	public static class PutHtmlFormatter extends HtmlFormatter<PutToSell>
	{
		private static final String SCHWAB = "https://client.schwab.com/Areas/Trade/Options/Chains/Index.aspx#symbol/%s";
		private static final String YAHOO = "https://finance.yahoo.com/quote/%s";

		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Schwab", "%s", Align.L));
			columns.add(new Column("Yahoo", "%s", Align.L));
			columns.add(new Column("Avail", "%.0f", Align.R));
			columns.add(new Column("O", "%d", Align.R));
			columns.add(new Column("Price", "%.2f", Align.R));
			columns.add(new Column("Day", "%s", Align.R));
			columns.add(new Column("Return", "%.0f%%", Align.R));
			return columns;
		}

		@Override
		protected List<Object> getObjectElements(PutToSell p)
		{
			String schwabLink = toLinkSymbol(SCHWAB, p.symbol);
			String yahooLink = toLinkSymbol(YAHOO, p.symbol);
			String dayChangePct = p.dayChangePct != null ? color(p.dayChangePct.doubleValue(), "%.2f%%") : null;
			Number open = p.openOrderQuantity != 0 ? p.openOrderQuantity : null;
			return Arrays.asList(schwabLink, yahooLink, p.availableAmount, open, p.underlyingPrice, dayChangePct, p.averageReturn);
		}
	}
}