package com.roddyaj.invest.programs.positions;

import java.util.ArrayList;
import java.util.List;

import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.util.HtmlFormatter;

public class Order
{
	public final String symbol;

	public final int quantity;

	public final double price;

	// Note, this may be null if there is no position
	public final Position position;

	public boolean optional;

	public Order(String symbol, int quantity, double price, Position position)
	{
		this.symbol = symbol;
		this.quantity = quantity;
		this.price = price;
		this.position = position;
	}

	public double getAmount()
	{
		return quantity * price;
	}

	@Override
	public String toString()
	{
		String action = quantity >= 0 ? green("Buy ") : red("Sell");
		double dayChangePct = position != null ? position.dayChangePct : 0;
		return String.format("%-5s %s %2d | %6.2f = %4.0f, %s", symbol, action, Math.abs(quantity), price, getAmount(), color(dayChangePct));
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
			columns.add(new Column("Action", "%s", Align.C));
			columns.add(new Column("#", "%d", Align.R));
			columns.add(new Column("Price", "%.2f", Align.R));
			columns.add(new Column("Total", "%.0f", Align.R));
			columns.add(new Column("Day", "%s", Align.R));
			columns.add(new Column("Total", "%s", Align.R));
			return columns;
		}

		@Override
		protected List<Object> getObjectElements(Order o)
		{
			String action = o.quantity >= 0 ? "Buy" : "Sell";
			final String url = "https://client.schwab.com/Areas/Trade/Allinone/index.aspx?tradeaction=" + action + "&amp;Symbol=%s";
			String link = String.format(
					"<a href=\"" + url + "\" target=\"_blank\" onclick=\"navigator.clipboard.writeText('" + Math.abs(o.quantity) + "');\">%s</a>",
					o.symbol, o.symbol);
			String actionColored = color(action, action.equals("Buy") ? "green" : "red");
			String dayChangeColored = o.position != null ? color(o.position.dayChangePct, "%.2f%%") : "";
			String gainLossPctColored = o.position != null ? color(o.position.gainLossPct, "%.2f%%") : "";
			return List.of(link, actionColored, Math.abs(o.quantity), o.price, o.getAmount(), dayChangeColored, gainLossPctColored);
		}
	}
}
