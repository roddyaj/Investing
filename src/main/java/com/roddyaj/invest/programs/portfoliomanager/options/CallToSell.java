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
import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.model.Action;
import com.roddyaj.invest.model.OpenOrder;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.Transaction;

public class CallToSell implements Comparable<CallToSell>
{
	private final Position position;
	private final int quantity;

	public CallToSell(Position position, int quantity)
	{
		this.position = position;
		this.quantity = quantity;
	}

	@Override
	public int compareTo(CallToSell o)
	{
		return Double.compare(o.position.getDayChangePct(), position.getDayChangePct());
	}

	public static class CallHtmlFormatter extends DataFormatter<CallToSell>
	{
		private final Account account;

		public CallHtmlFormatter(Collection<? extends CallToSell> records, Account account)
		{
			super("Calls To Sell", null, records);
			this.account = account;
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
			List<OpenOrder> openOrders = account.getOpenOrders(c.position.getSymbol(), Action.SELL, 'C');
			String quantityText = c.quantity + OpenOrder.getPopupText(openOrders);
			String cost = Transaction.createCostPopup(c.position, account);
			String dayChange = HtmlUtils.formatPercentChange(c.position.getDayChangePct());
			String totalChange = HtmlUtils.formatPercentChange(c.position.getGainLossPct());
			return List.of(schwab, yahoo, quantityText, cost, c.position.getPrice(), dayChange, totalChange);
		}
	}
}
