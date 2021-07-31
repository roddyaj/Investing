package com.roddyaj.invest.programs.options;

import java.util.ArrayList;
import java.util.List;

import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.util.HtmlFormatter;

public class CallToSell implements Comparable<CallToSell>
{
	public final Position position;
	public final double costPerShare;
	public final int quantity;

	public CallToSell(Position position, int quantity)
	{
		this.position = position;
		this.costPerShare = position.costBasis / position.quantity;
		this.quantity = quantity;
	}

	@Override
	public String toString()
	{
		return String.format("%-4s %d %s (bought at $%5.2f)", position.symbol, quantity, position.dayChangePct >= 0 ? "Y" : " ", costPerShare);
	}

	@Override
	public int compareTo(CallToSell o)
	{
		return position.symbol.compareTo(o.position.symbol);
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
			columns.add(new Column("#", "%d", Align.R));
			columns.add(new Column("Cost", "%.2f", Align.R));
			columns.add(new Column("", "%s", Align.C));
			columns.add(new Column("Price", "%.2f", Align.R));
			columns.add(new Column("Day Chg", "%s", Align.R));
			return columns;
		}

		@Override
		protected List<Object> getObjectElements(CallToSell c)
		{
			String schwab = toLinkSymbol(SCHWAB, c.position.symbol);
			String yahoo = toLinkSymbol(YAHOO, c.position.symbol);
			String dir = color(c.position.price >= c.costPerShare ? "&#8599;" : "&#8600;", c.position.price >= c.costPerShare ? "green" : "red");
			String changeColored = color(c.position.dayChangePct, "%.2f%%");
			return List.of(schwab, yahoo, c.quantity, c.costPerShare, dir, c.position.price, changeColored);
		}
	}
}
