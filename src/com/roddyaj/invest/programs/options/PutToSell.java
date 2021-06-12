package com.roddyaj.invest.programs.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.roddyaj.invest.util.HtmlFormatter;

public class PutToSell implements Comparable<PutToSell>
{
	public final String symbol;
	public final double availableAmount;
	public final double underlyingPrice;
	public double averageReturn;

	public PutToSell(String symbol, double availableAmount, double underlyingPrice)
	{
		this.symbol = symbol;
		this.availableAmount = availableAmount;
		this.underlyingPrice = underlyingPrice;
	}

	@Override
	public String toString()
	{
		return String.format("%-4s $%4.0f available (%3.0f%% return)", symbol, availableAmount, averageReturn);
	}

	@Override
	public int compareTo(PutToSell o)
	{
		int result = Double.compare(o.averageReturn, averageReturn);
		if (result == 0)
			result = symbol.compareTo(o.symbol);
		return result;
	}

	public static List<String> toBlock(Collection<? extends PutToSell> puts, double availableToTrade)
	{
		String title = HtmlFormatter.toTitle("Candidate Puts To Sell");
		String info = String.format("<div style=\"margin-left: 8px;\">$%.2f available to trade</div>", availableToTrade);
		String heading = "<div class=\"heading\" style=\"display: flex; align-items: center;\">" + title + info + "</div>";
		return new PutHtmlFormatter().toBlockHtmlTitle(puts, heading);
	}

	public static class PutHtmlFormatter extends HtmlFormatter<PutToSell>
	{
		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Schwab", "%s", Align.L));
			columns.add(new Column("Yahoo", "%s", Align.L));
			columns.add(new Column("Max", "$%.0f", Align.R));
			columns.add(new Column("U. Price", "$%.2f", Align.R));
			columns.add(new Column("Avg. Return", "%.0f%%", Align.R));
			return columns;
		}

		@Override
		protected List<Object> getObjectElements(PutToSell p)
		{
			final String schwab = "https://client.schwab.com/Areas/Trade/Options/Chains/Index.aspx#symbol/%s";
			final String yahoo = "https://finance.yahoo.com/quote/%s";

			return List.of(toLinkSymbol(schwab, p.symbol), toLinkSymbol(yahoo, p.symbol), p.availableAmount, p.underlyingPrice, p.averageReturn);
		}
	}
}
