package com.roddyaj.invest.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.invest.util.HtmlFormatter;
import com.roddyaj.invest.util.StringUtils;

public class Position implements Comparable<Position>
{
	private static final String SYMBOL = "Symbol";
//	private static final String DESCRIPTION = "Description";
	private static final String QUANTITY = "Quantity";
	private static final String PRICE = "Price";
//	private static final String PRICE_CHANGE_$ = "Price Change $";
//	private static final String PRICE_CHANGE_PERCENT = "Price Change %";
	private static final String MARKET_VALUE = "Market Value";
//	private static final String DAY_CHANGE_$ = "Day Change $";
	private static final String DAY_CHANGE_PERCENT = "Day Change %";
	private static final String COST_BASIS = "Cost Basis";
//	private static final String GAIN_LOSS_$ = "Gain/Loss $";
	private static final String GAIN_LOSS_PERCENT = "Gain/Loss %";
//	private static final String REINVEST_DIVIDENDS = "Reinvest Dividends?";
//	private static final String CAPITAL_GAINS = "Capital Gains?";
//	private static final String PERCENT_OF_ACCOUNT = "% Of Account";
//	private static final String DIVIDEND_YIELD = "Dividend Yield";
//	private static final String LAST_DIVIDEND = "Last Dividend";
//	private static final String EX_DIVIDEND_DATE = "Ex-Dividend Date";
//	private static final String P_E_RATIO = "P/E Ratio";
//	private static final String _52_WEEK_LOW = "52 Week Low";
//	private static final String _52_WEEK_HIGH = "52 Week High";
//	private static final String VOLUME = "Volume";
	private static final String INTRINSIC_VALUE = "Intrinsic Value";
	private static final String IN_THE_MONEY = "In The Money";
	private static final String SECURITY_TYPE = "Security Type";

	public final String symbol;
	public final int quantity;
	private final double price;
	private final double marketValue;
	public final double dayChangePct;
	public final double costBasis;
	public final double gainLossPct;
	public final String securityType;

	public final Option option;

	public Position(CSVRecord record)
	{
		String symbolOrOption = record.get(SYMBOL);
		quantity = StringUtils.parseInt(record.get(QUANTITY));
		price = StringUtils.parsePrice(record.get(PRICE));
		marketValue = StringUtils.parsePrice(record.get(MARKET_VALUE));
		dayChangePct = StringUtils.parsePercent(record.get(DAY_CHANGE_PERCENT));
		costBasis = StringUtils.parsePrice(record.get(COST_BASIS));
		gainLossPct = StringUtils.parsePercent(record.get(GAIN_LOSS_PERCENT));
		double intrinsicValue = record.isSet(INTRINSIC_VALUE) ? StringUtils.parseDouble(record.get(INTRINSIC_VALUE)) : 0;
		String money = record.isSet(IN_THE_MONEY) ? record.get(IN_THE_MONEY) : null;
		securityType = record.isSet(SECURITY_TYPE) ? record.get(SECURITY_TYPE) : null;

		if ("Option".equals(securityType))
		{
			option = new Option(symbolOrOption, money, intrinsicValue);
			symbol = option.symbol;
		}
		else
		{
			option = null;
			symbol = symbolOrOption;
		}
	}

	public Position(String symbol, int quantity, double price)
	{
		this.symbol = symbol;
		this.quantity = quantity;
		this.price = price;
		marketValue = quantity * price;
		dayChangePct = 0;
		costBasis = 0;
		gainLossPct = 0;
		option = null;
		securityType = null;
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

	public double getPrice()
	{
		return price;
	}

	public double getMarketValue()
	{
		return marketValue;
	}

	public double getMoneyInPlay()
	{
		return option != null ? quantity * (option.type == 'P' ? option.strike : option.getUnderlyingPrice()) * -100 : marketValue;
	}

	public double getOptionValueRatio()
	{
		double linearValue = ((double)option.getDteCurrent() / option.getDteOriginal()) * (costBasis + (quantity * .65));
		return marketValue / linearValue;
	}

	public double getCostPerShare()
	{
		return costBasis / quantity;
	}

//	@Override
//	public String toString()
//	{
//		return isOption() ? toStringOption() : toStringStock();
//	}
//
//	private String toStringStock()
//	{
//		return String.format("%-5s %3d %7.2f %7.2f %7.2f", symbol, quantity, marketValue, costBasis, dayChangePct);
//	}
//
//	private String toStringOption()
//	{
//		String moneyText = "OTM".equals(option.money) ? " " : "*";
//		return String.format("%-5s %2d %s %5.2f %s %s", symbol, quantity, option.expiryDate, option.strike, option.type, moneyText);
//	}

	@Override
	public String toString()
	{
		return symbol + ", quantity=" + quantity + ", price=" + price + ", marketValue=" + marketValue + ", dayChangePct=" + dayChangePct
				+ ", costBasis=" + costBasis + ", option=[" + option + "], getMoneyInPlay=" + getMoneyInPlay();
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
			return List.of(p.symbol, p.quantity, p.marketValue, p.costBasis, p.dayChangePct);
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
