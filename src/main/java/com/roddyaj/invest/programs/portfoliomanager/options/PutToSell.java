package com.roddyaj.invest.programs.portfoliomanager.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.roddyaj.invest.html.Block;
import com.roddyaj.invest.html.Table;
import com.roddyaj.invest.html.Table.Align;
import com.roddyaj.invest.html.Table.Column;
import com.roddyaj.invest.model.OpenOrder;
import com.roddyaj.invest.util.HtmlFormatter;

public class PutToSell implements Comparable<PutToSell>
{
	private final String symbol;
	private final double availableAmount;
	private final Double underlyingPrice;
	private final Double dayChangePct;
	private double averageReturn;
	private List<OpenOrder> openOrders;

	public PutToSell(String symbol, double availableAmount, Double underlyingPrice, Double dayChangePct)
	{
		this.symbol = symbol;
		this.availableAmount = availableAmount;
		this.underlyingPrice = underlyingPrice;
		this.dayChangePct = dayChangePct;
	}

	public void setAverageReturn(double averageReturn)
	{
		this.averageReturn = averageReturn;
	}

	public void setOpenOrders(List<OpenOrder> openOrders)
	{
		this.openOrders = openOrders;
	}

	public String getSymbol()
	{
		return symbol;
	}

	@Override
	public int compareTo(PutToSell o)
	{
		int result = Double.compare(dayChangePct != null ? dayChangePct.doubleValue() : 0, o.dayChangePct != null ? o.dayChangePct.doubleValue() : 0);
		if (result == 0)
		{
			result = Double.compare(o.averageReturn, averageReturn);
			if (result == 0)
				result = symbol.compareTo(o.symbol);
		}
		return result;
	}

	public static class PutHtmlFormatter
	{
		private static final String SCHWAB = "https://client.schwab.com/Areas/Trade/Options/Chains/Index.aspx#symbol/%s";
		private static final String YAHOO = "https://finance.yahoo.com/quote/%s";

		public static Block toBlock(Collection<? extends PutToSell> putsToSell, double availableToTrade)
		{
			String info = String.format("$%.0f available", availableToTrade);
			Table table = new Table(getColumns(), getRows(putsToSell));
			return new Block("Candidate Puts To Sell", info, table);
		}

		private static List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Schwab", "%s", Align.L));
			columns.add(new Column("Yahoo", "%s", Align.L));
			columns.add(new Column("Avail", "%.0f", Align.R));
			columns.add(new Column("O", "%s", Align.R));
			columns.add(new Column("Price", "%.2f", Align.R));
			columns.add(new Column("Day", "%s", Align.R));
			columns.add(new Column("Return", "%.0f%%", Align.R));
			return columns;
		}

		private static List<List<Object>> getRows(Collection<? extends PutToSell> putsToSell)
		{
			return putsToSell.stream().map(PutHtmlFormatter::toRow).collect(Collectors.toList());
		}

		private static List<Object> toRow(PutToSell p)
		{
			String schwabLink = HtmlFormatter.toLinkSymbol(SCHWAB, p.symbol);
			String yahooLink = HtmlFormatter.toLinkSymbol(YAHOO, p.symbol);
			String dayChangePct = p.dayChangePct != null ? HtmlFormatter.color(p.dayChangePct.doubleValue(), "%.2f%%") : null;
			String quantityText = OpenOrder.getPopupText(p.openOrders);
			return Arrays.asList(schwabLink, yahooLink, p.availableAmount, quantityText, p.underlyingPrice, dayChangePct, p.averageReturn);
		}
	}
}
