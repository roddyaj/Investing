package com.roddyaj.invest.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.roddyaj.invest.html.DataFormatter;
import com.roddyaj.invest.html.HtmlFormatter;
import com.roddyaj.invest.html.Table.Align;
import com.roddyaj.invest.html.Table.Column;

public class Order
{
	private final String symbol;

	private final int quantity;

	private final double price;

	// Note, this may be null if there is no position
	private final Position position;

	private List<OpenOrder> openOrders;

	private boolean optional;

	public Order(String symbol, int quantity, double price, Position position)
	{
		this.symbol = symbol;
		this.quantity = quantity;
		this.price = price;
		this.position = position;
	}

	public void setOptional(boolean optional)
	{
		this.optional = optional;
	}

	public void setOpenOrders(List<OpenOrder> openOrders)
	{
		this.openOrders = openOrders;
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

	public Position getPosition()
	{
		return position;
	}

	public boolean isOptional()
	{
		return optional;
	}

	public double getAmount()
	{
		return quantity * price;
	}

	@Override
	public String toString()
	{
		return "symbol=" + symbol + ", quantity=" + quantity + ", price=" + price;
	}

	public static class OrderFormatter extends DataFormatter<Order>
	{
		public OrderFormatter(String title, String info, Collection<? extends Order> records)
		{
			super(title, info, records);
		}

		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Ticker", "%s", Align.L));
			columns.add(new Column("Action", "%s", Align.C));
			columns.add(new Column("#", "%s", Align.L));
			columns.add(new Column("Price", "%.2f", Align.R));
			columns.add(new Column("Total", "%.0f", Align.R));
			columns.add(new Column("Day", "%s", Align.R));
			columns.add(new Column("Total", "%s", Align.R));
			return columns;
		}

		@Override
		protected List<Object> toRow(Order o)
		{
			String action = o.quantity >= 0 ? "Buy" : "Sell";
			final String url = "https://client.schwab.com/Areas/Trade/Allinone/index.aspx?tradeaction=" + action + "&amp;Symbol=%s";
			String link = String.format(
					"<a href=\"" + url + "\" target=\"_blank\" onclick=\"navigator.clipboard.writeText('" + Math.abs(o.quantity) + "');\">%s</a>",
					o.symbol, o.symbol);
			String quantityText = String.valueOf(Math.abs(o.quantity)) + OpenOrder.getPopupText(o.openOrders);
			String dayChangeColored = o.position != null ? HtmlFormatter.formatPercentChange(o.position.getDayChangePct()) : "";
			String gainLossPctColored = o.position != null ? HtmlFormatter.formatPercentChange(o.position.getGainLossPct()) : "";
			return List.of(link, action, quantityText, o.price, o.getAmount(), dayChangeColored, gainLossPctColored);
		}
	}
}
