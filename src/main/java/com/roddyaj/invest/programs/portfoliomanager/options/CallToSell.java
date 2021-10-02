package com.roddyaj.invest.programs.portfoliomanager.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.roddyaj.invest.html.DataFormatter;
import com.roddyaj.invest.html.HtmlFormatter;
import com.roddyaj.invest.html.Table.Align;
import com.roddyaj.invest.html.Table.Column;
import com.roddyaj.invest.model.OpenOrder;
import com.roddyaj.invest.model.Position;

public class CallToSell implements Comparable<CallToSell>
{
	private final Position position;
	private final int quantity;
	private List<OpenOrder> openOrders;

	public CallToSell(Position position, int quantity)
	{
		this.position = position;
		this.quantity = quantity;
	}

	public void setOpenOrders(List<OpenOrder> openOrders)
	{
		this.openOrders = openOrders;
	}

	@Override
	public int compareTo(CallToSell o)
	{
		return Double.compare(o.position.getDayChangePct(), position.getDayChangePct());
	}

	public static class CallHtmlFormatter extends DataFormatter<CallToSell>
	{
		private static final String SCHWAB = "https://client.schwab.com/Areas/Trade/Options/Chains/Index.aspx#symbol/%s";
		private static final String YAHOO = "https://finance.yahoo.com/quote/%s";

		public CallHtmlFormatter(Collection<? extends CallToSell> records)
		{
			super("Calls To Sell", null, records);
		}

		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Schwab", "%s", Align.L));
			columns.add(new Column("Yahoo", "%s", Align.L));
			columns.add(new Column("#", "%s", Align.L));
			columns.add(new Column("Cost", "%.2f", Align.R));
			columns.add(new Column("", "%s", Align.C));
			columns.add(new Column("Price", "%.2f", Align.R));
			columns.add(new Column("Day", "%s", Align.R));
			return columns;
		}

		@Override
		protected List<Object> toRow(CallToSell c)
		{
			String schwab = HtmlFormatter.toLinkSymbol(SCHWAB, c.position.getSymbol());
			String yahoo = HtmlFormatter.toLinkSymbol(YAHOO, c.position.getSymbol());
			String quantityText = c.quantity + OpenOrder.getPopupText(c.openOrders);
			double costPerShare = c.position.getCostPerShare();
			String dir = HtmlFormatter.color(c.position.getPrice() >= costPerShare ? "&#8599;" : "&#8600;",
					c.position.getPrice() >= costPerShare ? "green" : "red");
			String changeColored = HtmlFormatter.color(c.position.getDayChangePct(), "%.2f%%");
			return List.of(schwab, yahoo, quantityText, costPerShare, dir, c.position.getPrice(), changeColored);
		}
	}
}
