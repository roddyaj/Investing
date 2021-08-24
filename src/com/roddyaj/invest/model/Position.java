package com.roddyaj.invest.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.roddyaj.invest.util.HtmlFormatter;

public class Position implements Comparable<Position>
{
	private final String symbol;
	private final int quantity;
	private final double price;
	private final double marketValue;
	private final SecurityType securityType;
	private final double costBasis;
	private final double dayChangePct;
	private final double gainLossPct;

	private final Option option;

	public Position(String symbol, int quantity, double price, double marketValue, SecurityType securityType, double costBasis, double dayChangePct,
			double gainLossPct, Option option)
	{
		this.symbol = symbol;
		this.quantity = quantity;
		this.price = price;
		this.marketValue = marketValue;
		this.securityType = securityType;
		this.costBasis = costBasis;
		this.dayChangePct = dayChangePct;
		this.gainLossPct = gainLossPct;
		this.option = option;
	}

	public String getSymbol()
	{
		return symbol;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public double getPrice()
	{
		return price;
	}

	public double getMarketValue()
	{
		return marketValue;
//		return quantity * price;
	}

	public SecurityType getSecurityType()
	{
		return securityType;
	}

	public double getCostBasis()
	{
		return costBasis;
	}

	public double getDayChangePct()
	{
		return dayChangePct;
	}

	public double getGainLossPct()
	{
		return gainLossPct;
	}

	public Option getOption()
	{
		return option;
	}

	public double getCostPerShare()
	{
		return costBasis / quantity;
	}

	public boolean isOption()
	{
		return option != null;
	}

	public boolean isCallOption()
	{
		return isOption() && option.type == 'C';
	}

	public boolean isPutOption()
	{
		return isOption() && option.type == 'P';
	}

	public double getMoneyInPlay()
	{
		return option != null ? quantity * (option.type == 'P' ? option.strike : option.getUnderlyingPrice()) * -100 : getMarketValue();
	}

//	public double getOptionValueRatio()
//	{
//		double linearValue = ((double)option.getDteCurrent() / option.getDteOriginal()) * (costBasis + (quantity * .65));
//		return marketValue / linearValue;
//	}

	@Override
	public String toString()
	{
		return symbol + ", quantity=" + quantity + ", price=" + price + ", marketValue=" + getMarketValue() + ", dayChangePct=" + dayChangePct
				+ ", costBasis=" + costBasis + ", option=[" + option + "], moneyInPlay=" + getMoneyInPlay();
	}

	@Override
	public int compareTo(Position o)
	{
		return symbol.compareTo(o.symbol);
	}

	public static class StockHtmlFormatter extends HtmlFormatter<Position>
	{
		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Ticker", "%s", Align.L));
			columns.add(new Column("#", "%d", Align.R));
			columns.add(new Column("MarketValue", "$%.2f", Align.R));
			columns.add(new Column("Cost Basis", "$%.2f", Align.R));
			columns.add(new Column("Day Change", "%.2f%%", Align.R));
			return columns;
		}

		@Override
		protected List<Object> getObjectElements(Position p)
		{
			return List.of(p.symbol, p.quantity, p.getMarketValue(), p.costBasis, p.dayChangePct);
		}
	}

	public static class OptionHtmlFormatter extends HtmlFormatter<Position>
	{
		private static final String URL = "https://client.schwab.com/Areas/Trade/Allinone/index.aspx#symbol/";

		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Ticker", "%s", Align.L));
			columns.add(new Column("#", "%d", Align.R));
			columns.add(new Column("", "%s", Align.C));
			columns.add(new Column("Expiration", "%s", Align.L));
			columns.add(new Column("DTE", "%d", Align.R));
			columns.add(new Column("Strike", "%.2f", Align.R));
			columns.add(new Column("Price", "%.2f", Align.R));
//			columns.add(new Column("Cost", "%.2f", Align.R));
			columns.add(new Column("", "%s", Align.C));
//			columns.add(new Column("Return", "%.0f%%", Align.R));
			return columns;
		}

		@Override
		protected List<Object> getObjectElements(Position p)
		{
			String link = toLink(URL + p.option.toOccString().replace(' ', '+'), p.symbol);
			int dte = p.option.getDteCurrent();
//			Double underlyingCostPerShare = p.option.underlying != null ? p.option.underlying.getCostPerShare() : null;
			String moneyText = "OTM".equals(p.option.money) ? "" : dte < 5 ? "**" : "*";
//			double annualReturn = ((p.costBasis / p.quantity) / p.option.strike) * (365.0 / dte);

			return Arrays.asList(link, p.quantity, p.option.type, p.option.expiryDate, dte, p.option.strike, p.option.getUnderlyingPrice(),
					moneyText);
		}
	}
}
