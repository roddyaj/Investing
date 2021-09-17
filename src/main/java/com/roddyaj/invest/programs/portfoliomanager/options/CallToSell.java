package com.roddyaj.invest.programs.portfoliomanager.options;

import java.util.ArrayList;
import java.util.List;

import com.roddyaj.invest.model.OpenOrder;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.util.HtmlFormatter;

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

	public static class CallHtmlFormatter extends HtmlFormatter<CallToSell>
	{
		private static final String SCHWAB = "https://client.schwab.com/Areas/Trade/Options/Chains/Index.aspx#symbol/%s";
		private static final String YAHOO = "https://finance.yahoo.com/quote/%s";

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
		protected List<Object> getObjectElements(CallToSell c)
		{
			String schwab = toLinkSymbol(SCHWAB, c.position.getSymbol());
			String yahoo = toLinkSymbol(YAHOO, c.position.getSymbol());
			String quantityText = c.quantity + OpenOrder.getPopupText(c.openOrders);
			double costPerShare = c.position.getCostPerShare();
			String dir = color(c.position.getPrice() >= costPerShare ? "&#8599;" : "&#8600;",
					c.position.getPrice() >= costPerShare ? "green" : "red");
			String changeColored = color(c.position.getDayChangePct(), "%.2f%%");
			return List.of(schwab, yahoo, quantityText, costPerShare, dir, c.position.getPrice(), changeColored);
		}
	}
}
