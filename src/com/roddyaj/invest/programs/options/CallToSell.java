package com.roddyaj.invest.programs.options;

import java.util.ArrayList;
import java.util.List;

import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.util.HtmlFormatter;

public class CallToSell implements Comparable<CallToSell>
{
	public final Position position;
	public final int quantity;

	public CallToSell(Position position, int quantity)
	{
		this.position = position;
		this.quantity = quantity;
	}

//	@Override
//	public String toString()
//	{
//		return String.format("%-4s %d %s (bought at $%5.2f)", position.symbol, quantity, position.dayChangePct >= 0 ? "Y" : " ",
//				position.getCostPerShare());
//	}

	@Override
	public int compareTo(CallToSell o)
	{
		return Double.compare(o.position.dayChangePct, position.dayChangePct);
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
			columns.add(new Column("Day", "%s", Align.R));
			return columns;
		}

		@Override
		protected List<Object> getObjectElements(CallToSell c)
		{
			String schwab = toLinkSymbol(SCHWAB, c.position.symbol);
			String yahoo = toLinkSymbol(YAHOO, c.position.symbol);
			double costPerShare = c.position.getCostPerShare();
			String dir = color(c.position.price >= costPerShare ? "&#8599;" : "&#8600;", c.position.price >= costPerShare ? "green" : "red");
			String changeColored = color(c.position.dayChangePct, "%.2f%%");
			return List.of(schwab, yahoo, c.quantity, costPerShare, dir, c.position.price, changeColored);
		}
	}
}
