package com.roddyaj.invest.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.roddyaj.invest.api.schwab.SchwabDataSource;
import com.roddyaj.invest.html.DataFormatter;
import com.roddyaj.invest.html.HtmlUtils;
import com.roddyaj.invest.html.Table.Align;
import com.roddyaj.invest.html.Table.Column;

public class Order
{
	private final String symbol;

	private final int quantity;

	private final double price;

	// Note, this may be null if there is no position
	private final Position position;

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
		private final Account account;

		public OrderFormatter(String title, String info, Collection<? extends Order> records, Account account)
		{
			super(title, info, records);
			this.account = account;
		}

		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Ticker", "%s", Align.L));
			columns.add(new Column("", "%s", Align.C));
			columns.add(new Column("#", "%s", Align.L));
			columns.add(new Column("Price", "%.2f", Align.R));
			columns.add(new Column("Total", "%.0f", Align.R));
			columns.add(new Column("Day", "%s", Align.R));
			columns.add(new Column("Total", "%s", Align.R));
			columns.add(new Column("Cost", "%s", Align.R));
			return columns;
		}

		@Override
		protected List<Object> toRow(Order o)
		{
			boolean isBuy = o.quantity >= 0;

			String url = SchwabDataSource.getTradeUrl(isBuy ? Action.BUY : Action.SELL, o.symbol);
			String link = HtmlUtils.toLink(url, o.symbol,
					Map.of("onclick", String.format("navigator.clipboard.writeText('%d');", Math.abs(o.quantity))));

			List<OpenOrder> openOrders = account.getOpenOrders(o.getSymbol(), o.getQuantity() >= 0 ? Action.BUY : Action.SELL, null);
			String quantityText = String.valueOf(Math.abs(o.quantity)) + OpenOrder.getPopupText(openOrders);
			String dayChangeColored = o.position != null ? HtmlUtils.formatPercentChange(o.position.getDayChangePct()) : "";
			String gainLossPctColored = o.position != null ? HtmlUtils.formatPercentChange(o.position.getGainLossPct()) : "";
			String cost = o.position != null ? Transaction.createCostPopup(o.position, account) : "";

			return Arrays.asList(link, isBuy ? "Buy" : "Sell", quantityText, o.price, o.getAmount(), dayChangeColored, gainLossPctColored, cost);
		}
	}
}
