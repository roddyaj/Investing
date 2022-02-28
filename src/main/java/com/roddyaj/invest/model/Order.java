package com.roddyaj.invest.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.roddyaj.invest.api.schwab.SchwabDataSource;
import com.roddyaj.invest.api.yahoo.YahooUtils;
import com.roddyaj.invest.html.DataFormatter;
import com.roddyaj.invest.html.HtmlUtils;
import com.roddyaj.invest.html.Table.Align;
import com.roddyaj.invest.html.Table.Column;

public record Order(String symbol, int quantity, double price, CompletePosition completePosition, boolean optional)
{
	public double getAmount()
	{
		return quantity * price;
	}

	public static class OrderFormatter extends DataFormatter<Order>
	{
		public OrderFormatter(String title, String info, Collection<? extends Order> records)
		{
			super(title, info, records);
		}

		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Ticker", "%s", Align.L));
			columns.add(new Column("", "%s", Align.L));
			columns.add(new Column("", "%s", Align.C));
			columns.add(new Column("#", "%s", Align.L));
			columns.add(new Column("Price", "%.2f", Align.R));
			columns.add(new Column("Total", "%.0f", Align.R));
			columns.add(new Column("Day", "%s", Align.R));
			columns.add(new Column("G/L", "%s", Align.R));
			columns.add(new Column("Cost", "%s", Align.R));
			return columns;
		}

		@Override
		protected List<Object> toRow(Order o)
		{
			Position position = o.completePosition != null ? o.completePosition.getPosition() : null;
			Action action = o.quantity >= 0 ? Action.BUY : Action.SELL;

			String url = SchwabDataSource.getTradeUrl(action, o.symbol);
			String link = HtmlUtils.toLink(url, o.symbol, Map.of("onclick", String.format("copyClip('%d');", Math.abs(o.quantity))));
			if (o.completePosition != null) {
				link = new PositionPopup(o.completePosition).createPopup(link);
			}
			String yahoo = YahooUtils.getIconLink(o.symbol);

			List<OpenOrder> openOrders = o.completePosition != null
				? o.completePosition.getOpenOrders().stream().filter(oo -> (oo.quantity() > 0) == (o.quantity() > 0)).toList()
				: List.of();
			String quantityText = String.valueOf(Math.abs(o.quantity)) + OpenOrder.getPopupText(openOrders);
			String dayChangeColored = position != null ? HtmlUtils.formatPercentChange(position.getDayChangePct()) : "";
			String gainLossPctColored = position != null ? HtmlUtils.formatPercentChange(position.getGainLossPct()) : "";
			String cost = position != null ? String.format("%.2f", position.getCostPerShare()) : "";

			return Arrays.asList(link, yahoo, action.toString(), quantityText, o.price, o.getAmount(), dayChangeColored, gainLossPctColored, cost);
		}
	}
}
