package com.roddyaj.invest.programs.options;

import java.util.ArrayList;
import java.util.List;

import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.util.HtmlFormatter;

public class CallToSell
{
	public final Position position;
	public final double costPerShare;
	public final int quantity;

	public CallToSell(Position position, double costPerShare, int quantity)
	{
		this.position = position;
		this.costPerShare = costPerShare;
		this.quantity = quantity;
	}

	@Override
	public String toString()
	{
		return String.format("%-4s %d %s (bought at $%5.2f)", position.symbol, quantity, position.dayChangePct >= 0 ? "Y" : " ", costPerShare);
	}

	public static class CallHtmlFormatter extends HtmlFormatter<CallToSell>
	{
		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Schwab", "%s", Align.L));
			columns.add(new Column("Yahoo", "%s", Align.L));
			columns.add(new Column("#", "%d", Align.R));
			columns.add(new Column("Favorable", "%s", Align.C));
			columns.add(new Column("Cost/Share", "$%.2f", Align.R));
			return columns;
		}

		@Override
		protected List<Object> getObjectElements(CallToSell c)
		{
			final String schwab = "https://client.schwab.com/Areas/Trade/Options/Chains/Index.aspx#symbol/%s";
			final String yahoo = "https://finance.yahoo.com/quote/%s";

			return List.of(toLinkSymbol(schwab, c.position.symbol), toLinkSymbol(yahoo, c.position.symbol), c.quantity,
					c.position.dayChangePct >= 0 ? "Y" : "", c.costPerShare);
		}
	}
}
