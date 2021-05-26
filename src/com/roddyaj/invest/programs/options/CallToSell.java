package com.roddyaj.invest.programs.options;

import java.util.ArrayList;
import java.util.List;

import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.util.HtmlFormatter;

public class CallToSell
{
	public final Position position;
	public final double lastBuy;
	public final int quantity;

	public CallToSell(Position position, double lastBuy, int quantity)
	{
		this.position = position;
		this.lastBuy = lastBuy;
		this.quantity = quantity;
	}

	@Override
	public String toString()
	{
		return String.format("%-4s %d %s (bought at $%5.2f)", position.symbol, quantity, position.dayChangePct >= 0 ? "Y" : " ", lastBuy);
	}

	public static class CallHtmlFormatter extends HtmlFormatter<CallToSell>
	{
		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Ticker", "%s", Align.L));
			columns.add(new Column("#", "%d", Align.R));
			columns.add(new Column("Favorable", "%s", Align.C));
			columns.add(new Column("Last Buy", "$%.2f", Align.R));
			return columns;
		}

		@Override
		protected List<Object> getObjectElements(CallToSell c)
		{
			final String url = "https://client.schwab.com/Areas/Trade/Options/Chains/Index.aspx#symbol/%s";
			String link = String.format("<a href=\"" + url + "\">%s</a>", c.position.symbol, c.position.symbol);

			return List.of(link, c.quantity, c.position.dayChangePct >= 0 ? "Y" : "", c.lastBuy);
		}
	}
}
