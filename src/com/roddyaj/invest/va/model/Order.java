package com.roddyaj.invest.va.model;

import java.util.ArrayList;
import java.util.List;

import com.roddyaj.invest.util.HtmlFormatter;

public class Order
{
	public final String symbol;

	public final int shareCount;

	public final double price;

	// TODO doesn't really belong here
	public final double changePct;

	public Order(String symbol, int shareCount, double price, double changePct)
	{
		this.symbol = symbol;
		this.shareCount = shareCount;
		this.price = price;
		this.changePct = changePct;
	}

	public double getAmount()
	{
		return shareCount * price;
	}

	@Override
	public String toString()
	{
		String action = shareCount >= 0 ? green("Buy ") : red("Sell");
		return String.format("%-4s %s %2d | %6.2f = %4.0f, %s", symbol, action, Math.abs(shareCount), price, getAmount(), color(changePct));
	}

	private static String color(double d)
	{
		String s = String.format("%.2f", d);
		return d >= 0 ? green(" " + s) : red(s);
	}

	private static String red(String s)
	{
		return "\033[31m" + s + "\033[0m";
	}

	private static String green(String s)
	{
		return "\033[32m" + s + "\033[0m";
	}

	public static class OrderFormatter extends HtmlFormatter<Order>
	{
		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Ticker", "%s", Align.L));
			columns.add(new Column("Action", "%s", Align.L));
			columns.add(new Column("#", "%d", Align.R));
			columns.add(new Column("Price", "%.2f", Align.R));
			columns.add(new Column("Amount", "%.0f", Align.R));
			columns.add(new Column("Change", "%s", Align.R));
			return columns;
		}

		@Override
		protected List<Object> getObjectElements(Order o)
		{
			String action = o.shareCount >= 0 ? "Buy" : "Sell";
			final String url = "https://client.schwab.com/Areas/Trade/Allinone/index.aspx?tradeaction=" + action + "&amp;Symbol=%s";
			return List.of(toLink(url, o.symbol), action, Math.abs(o.shareCount), o.price, o.getAmount(), color(o.changePct, "%.2f%%"));
		}

		private static String color(double d, String format)
		{
			return "<font color=\"" + (d >= 0 ? "green" : "red") + "\">" + String.format(format, d) + "</font>";
		}
	}
}
