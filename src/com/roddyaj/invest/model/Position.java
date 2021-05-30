package com.roddyaj.invest.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.invest.util.HtmlFormatter;
import com.roddyaj.invest.util.StringUtils;

public class Position implements Comparable<Position>
{
	public final String symbol;
	public final int quantity;
	public final double price;
	public final double marketValue;
	public final double dayChangePct;
	public final double costBasis;

	public final Option option;

	public Position(CSVRecord record)
	{
		String symbolOrOption = record.get(0);
		quantity = StringUtils.parseInt(record.get(2));
		price = StringUtils.parsePrice(record.get(3));
		marketValue = StringUtils.parsePrice(record.get(6));
		dayChangePct = StringUtils.parsePercent(record.get(8));
		costBasis = StringUtils.parsePrice(record.get(9));
		double intrinsicValue = record.size() > 22 ? StringUtils.parseDouble(record.get(22)) : 0;
		String money = record.size() > 23 ? record.get(23) : null;
		String securityType = record.size() > 24 ? record.get(24) : null;

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
		option = null;
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

	@Override
	public String toString()
	{
		return isOption() ? toStringOption() : toStringStock();
	}

	private String toStringStock()
	{
		return String.format("%-5s %3d %7.2f %7.2f %7.2f", symbol, quantity, marketValue, costBasis, dayChangePct);
	}

	private String toStringOption()
	{
		String moneyText = "OTM".equals(option.money) ? " " : "*";
		return String.format("%-5s %2d %s %5.2f %s %s", symbol, quantity, option.expiryDate, option.strike, option.type, moneyText);
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
		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Ticker", "%s", Align.L));
			columns.add(new Column("#", "%d", Align.R));
			columns.add(new Column("Expiration", "%s", Align.L));
			columns.add(new Column("Days", "%d", Align.R));
			columns.add(new Column("Strike", "$%.2f", Align.R));
			columns.add(new Column("Type", "%s", Align.C));
			columns.add(new Column("", "%s", Align.C));
			return columns;
		}

		@Override
		protected List<Object> getObjectElements(Position p)
		{
			final String url = "https://client.schwab.com/Areas/Trade/Options/Chains/Index.aspx#symbol/%s";
			int days = (int)ChronoUnit.DAYS.between(LocalDate.now(), p.option.expiryDate);
			String moneyText = "OTM".equals(p.option.money) ? "" : "*";

			return List.of(toLink(url, p.symbol), p.quantity, p.option.expiryDate, days, p.option.strike, p.option.type, moneyText);
		}
	}
}
