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
import com.roddyaj.invest.model.CompletePosition;
import com.roddyaj.invest.model.OpenOrder;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.PositionPopup;

public class CallToSell implements Comparable<CallToSell>
{
	private final CompletePosition completePosition;
	private final Position position;
	private final int quantity;

	public CallToSell(CompletePosition completePosition, int quantity)
	{
		this.completePosition = completePosition;
		this.position = completePosition.getPosition();
		this.quantity = quantity;
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
			columns.add(new Column("G/L", "%s", Align.R));
			return columns;
		}

		@Override
		protected List<Object> toRow(CallToSell c)
		{
			String schwab = HtmlUtils.toLink(SchwabDataSource.getOptionChainsUrl(c.position.getSymbol()), c.position.getSymbol());
			String yahoo = YahooUtils.getIconLink(c.position.getSymbol());
			List<OpenOrder> openOrders = c.completePosition.getOpenOrders().stream()
				.filter(o -> o.option() != null && o.option().getType() == 'C' && o.quantity() < 0).toList();
			String quantityText = c.quantity + OpenOrder.getPopupText(openOrders);
			String cost = new PositionPopup(c.completePosition).createCostPopup();
			String dayChange = HtmlUtils.formatPercentChange(c.position.getDayChangePct());
			String totalChange = HtmlUtils.formatPercentChange(c.position.getGainLossPct());
			return List.of(schwab, yahoo, quantityText, cost, c.position.getPrice(), dayChange, totalChange);
		}
	}
}
