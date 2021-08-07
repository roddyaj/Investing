package com.roddyaj.invest.programs.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.util.HtmlFormatter;

public class PutToSell implements Comparable<PutToSell>
{
	public final String symbol;
	public final double availableAmount;
	public final double underlyingPrice;
	public double averageReturn;
	// Note, this may be null
	public Position position;

	public PutToSell(String symbol, double availableAmount, double underlyingPrice, Position position)
	{
		this.symbol = symbol;
		this.availableAmount = availableAmount;
		this.underlyingPrice = underlyingPrice;
		this.position = position;
	}

	@Override
	public String toString()
	{
		return String.format("%-4s $%4.0f available (%3.0f%% return)", symbol, availableAmount, averageReturn);
	}

	@Override
	public int compareTo(PutToSell o)
	{
		int result = Double.compare(o.position != null ? o.position.dayChangePct : 0, position != null ? position.dayChangePct : 0);
		if (result == 0)
		{
			result = Double.compare(o.averageReturn, averageReturn);
			if (result == 0)
				result = symbol.compareTo(o.symbol);
		}
		return result;
	}

	public static List<String> toBlock(Collection<? extends PutToSell> puts, double availableToTrade)
	{
		String info = String.format("$%.0f available", availableToTrade);
		return new PutHtmlFormatter().toBlock(puts, "Candidate Puts To Sell", info);
	}

	public static class PutHtmlFormatter extends HtmlFormatter<PutToSell>
	{
		private static final String SCHWAB = "https://client.schwab.com/Areas/Trade/Options/Chains/Index.aspx#symbol/%s";
		private static final String YAHOO = "https://finance.yahoo.com/quote/%s";

		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Schwab", "%s", Align.L));
			columns.add(new Column("Yahoo", "%s", Align.L));
			columns.add(new Column("Avail", "%.0f", Align.R));
			columns.add(new Column("Price", "%.2f", Align.R));
			columns.add(new Column("Day", "%s", Align.R));
			columns.add(new Column("Return", "%.0f%%", Align.R));
			return columns;
		}

		@Override
		protected List<Object> getObjectElements(PutToSell p)
		{
			String schwabLink = toLinkSymbol(SCHWAB, p.symbol);
			String yahooLink = toLinkSymbol(YAHOO, p.symbol);
			Double underlyingPrice = p.underlyingPrice != 0 ? p.underlyingPrice : null;
			String dayChangePct = p.position != null ? color(p.position.dayChangePct, "%.2f%%") : null;
			return Arrays.asList(schwabLink, yahooLink, p.availableAmount, underlyingPrice, dayChangePct, p.averageReturn);
		}
	}
}
