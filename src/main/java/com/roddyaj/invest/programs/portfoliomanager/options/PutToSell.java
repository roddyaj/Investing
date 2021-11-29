package com.roddyaj.invest.programs.portfoliomanager.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.roddyaj.invest.api.schwab.SchwabDataSource;
import com.roddyaj.invest.api.yahoo.YahooUtils;
import com.roddyaj.invest.html.DataFormatter;
import com.roddyaj.invest.html.HtmlFormatter;
import com.roddyaj.invest.html.Table.Align;
import com.roddyaj.invest.html.Table.Column;
import com.roddyaj.invest.model.OpenOrder;

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

	public static class PutHtmlFormatter extends DataFormatter<PutToSell>
	{
		public PutHtmlFormatter(Collection<? extends PutToSell> records, double availableToTrade)
		{
			super("Candidate Puts To Sell", String.format("$%.0f avail.", availableToTrade), records);
		}

		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Ticker", "%s", Align.L));
			columns.add(new Column("", "%s", Align.L));
			columns.add(new Column("Avail", "%.0f", Align.R));
			columns.add(new Column("O", "%s", Align.R));
			columns.add(new Column("Price", "%.2f", Align.R));
			columns.add(new Column("Day", "%s", Align.R));
			columns.add(new Column("Return", "%.0f%%", Align.R));
			return columns;
		}

		@Override
		protected List<Object> toRow(PutToSell p)
		{
			String schwabLink = HtmlFormatter.toLink(SchwabDataSource.getOptionChainsUrl(p.symbol), p.symbol);
			String yahooLink = YahooUtils.getIconLink(p.symbol);
			String openOrders = OpenOrder.getPopupText(p.openOrders);
			String dayChangePct = p.dayChangePct != null ? HtmlFormatter.formatPercentChange(p.dayChangePct.doubleValue()) : null;
			return Arrays.asList(schwabLink, yahooLink, p.availableAmount, openOrders, p.underlyingPrice, dayChangePct, p.averageReturn);
		}
	}
}
