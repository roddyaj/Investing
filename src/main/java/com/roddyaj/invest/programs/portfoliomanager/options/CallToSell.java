package com.roddyaj.invest.programs.portfoliomanager.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.roddyaj.invest.api.schwab.SchwabDataSource;
import com.roddyaj.invest.api.yahoo.YahooUtils;
import com.roddyaj.invest.html.DataFormatter;
import com.roddyaj.invest.html.HtmlUtils;
import com.roddyaj.invest.html.Table.Align;
import com.roddyaj.invest.html.Table.Column;
import com.roddyaj.invest.model.OpenOrder;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.Transaction;

public class CallToSell implements Comparable<CallToSell>
{
	private final Position position;
	private final int quantity;
	private List<OpenOrder> openOrders;
	private List<Transaction> transactions;

	public CallToSell(Position position, int quantity)
	{
		this.position = position;
		this.quantity = quantity;
	}

	public void setOpenOrders(List<OpenOrder> openOrders)
	{
		this.openOrders = openOrders;
	}

	public void setTransactions(List<Transaction> transactions)
	{
		this.transactions = transactions;
	}

	@Override
	public int compareTo(CallToSell o)
	{
		return Double.compare(o.position.getDayChangePct(), position.getDayChangePct());
	}

	public static class CallHtmlFormatter extends DataFormatter<CallToSell>
	{
		public CallHtmlFormatter(Collection<? extends CallToSell> records)
		{
			super("Calls To Sell", null, records);
		}

		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Ticker", "%s", Align.L));
			columns.add(new Column("", "%s", Align.L));
			columns.add(new Column("#", "%s", Align.L));
			columns.add(new Column("Cost", "%s", Align.R));
			columns.add(new Column("Price", "%.2f", Align.R));
			columns.add(new Column("Day", "%s", Align.R));
			columns.add(new Column("Total", "%s", Align.R));
			return columns;
		}

		@Override
		protected List<Object> toRow(CallToSell c)
		{
			String schwab = HtmlUtils.toLink(SchwabDataSource.getOptionChainsUrl(c.position.getSymbol()), c.position.getSymbol());
			String yahoo = YahooUtils.getIconLink(c.position.getSymbol());
			String quantityText = c.quantity + OpenOrder.getPopupText(c.openOrders);
			String cost = HtmlUtils.createPopup(String.format("%.2f", c.position.getCostPerShare()), Transaction.getPopupText(c.transactions), true);
			String dayChange = HtmlUtils.formatPercentChange(c.position.getDayChangePct());
			String totalChange = HtmlUtils.formatPercentChange(c.position.getGainLossPct());
			return List.of(schwab, yahoo, quantityText, cost, c.position.getPrice(), dayChange, totalChange);
		}
	}
}
