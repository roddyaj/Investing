package com.roddyaj.invest.model;

import java.util.ArrayList;
import java.util.List;

import com.roddyaj.invest.util.HtmlFormatter;

public class Order
{
	private final String symbol;

	private final int quantity;

	private final double price;

	// Note, this may be null if there is no position
	private final Position position;

	private int openOrderQuantity;

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

	public void setOpenOrderQuantity(int openOrderQuantity)
	{
		this.openOrderQuantity = openOrderQuantity;
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

	public int getOpenOrderQuantity()
	{
		return openOrderQuantity;
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

	public static class OrderFormatter extends HtmlFormatter<Order>
	{
		private static int orderId;

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
		protected List<Object> getObjectElements(Order o)
		{
			String action = o.quantity >= 0 ? "Buy" : "Sell";
			final String url = "https://client.schwab.com/Areas/Trade/Allinone/index.aspx?tradeaction=" + action + "&amp;Symbol=%s";
			String link = String.format(
					"<a href=\"" + url + "\" target=\"_blank\" onclick=\"navigator.clipboard.writeText('" + Math.abs(o.quantity) + "');\">%s</a>",
					o.symbol, o.symbol);
			String quantityText = String.valueOf(Math.abs(o.quantity));
			if (o.openOrderQuantity != 0)
			{
				String popupText = createPopup(String.valueOf(o.openOrderQuantity), String.valueOf(o.openOrderQuantity), "order-" + orderId);
				quantityText += " (" + popupText + ")";
			}
			orderId++;
			String dayChangeColored = o.position != null ? color(o.position.getDayChangePct(), "%.2f%%") : "";
			String gainLossPctColored = o.position != null ? color(o.position.getGainLossPct(), "%.2f%%") : "";
			return List.of(link, action, quantityText, o.price, o.getAmount(), dayChangeColored, gainLossPctColored);
		}
	}
}
