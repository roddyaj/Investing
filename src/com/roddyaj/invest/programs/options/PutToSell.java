package com.roddyaj.invest.programs.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.roddyaj.invest.util.HtmlFormatter;

public class PutToSell implements Comparable<PutToSell>
{
	public final String symbol;
	public final double availableAmount;
	public double averageReturn;

	public PutToSell(String symbol, double availableAmount)
	{
		this.symbol = symbol;
		this.availableAmount = availableAmount;
	}

	@Override
	public String toString()
	{
		return String.format("%-4s $%4.0f available (%3.0f%% return)", symbol, availableAmount, averageReturn);
	}

	@Override
	public int compareTo(PutToSell o)
	{
		return Double.compare(o.averageReturn, averageReturn);
	}

	public static List<String> toBlock(Collection<? extends PutToSell> puts, double availableToTrade)
	{
		String title = String.format("<div class=\"heading\"><b>Candidate Puts To Sell</b>&nbsp; $%.2f available to trade</div>", availableToTrade);
		return new PutHtmlFormatter().toBlockHtmlTitle(puts, title);
	}

	public static class PutHtmlFormatter extends HtmlFormatter<PutToSell>
	{
		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Schwab", "%s", Align.L));
			columns.add(new Column("Yahoo", "%s", Align.L));
			columns.add(new Column("Available", "$%.0f", Align.R));
			columns.add(new Column("Avg. Return", "%.0f%%", Align.R));
			return columns;
		}

		@Override
		protected List<Object> getObjectElements(PutToSell p)
		{
			final String schwab = "https://client.schwab.com/Areas/Trade/Options/Chains/Index.aspx#symbol/%s";
			final String yahoo = "https://finance.yahoo.com/quote/%s";

			return List.of(toLink(schwab, p.symbol), toLink(yahoo, p.symbol), p.availableAmount, p.averageReturn);
		}
	}
}
