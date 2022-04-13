package com.roddyaj.invest.model;

import static com.roddyaj.invest.html.HtmlUtils.div;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.roddyaj.invest.html.HtmlUtils;
import com.roddyaj.invest.html.Table;

public class PositionPopup
{
	private final CompletePosition completePosition;

	public PositionPopup(CompletePosition completePosition)
	{
		this.completePosition = completePosition;
	}

	public String createCostPopup()
	{
		String costText = String.format("%.2f", completePosition.getPosition().getCostPerShare());
		String costDiv = div(Map.of("style", "text-decoration: underline;"), costText);
		return createPopup(costDiv);
	}

	public String createPopup(String content)
	{
		return HtmlUtils.createPopup(content, getPopupContent(), true);
	}

	public String getPopupContent()
	{
		String topDiv = div(null, getTopContent());
		String transactionsDiv = div(null, getTransactionsContent());
		String ordersDiv = div(Map.of("style", "margin-left: 8px;"), getOrdersContent());
		String centerDiv = div(Map.of("class", "row", "style", "margin-top: 16px;"), transactionsDiv + ordersDiv);
		return div(Map.of("style", "text-align: left;"), topDiv + centerDiv);
	}

	private String getTopContent()
	{
		StringBuilder text = new StringBuilder();
		Position position = completePosition.getPosition();
		text.append(removeAfter(position.getDescription(), " ADR")).append("<br><br>");
		text.append(position.getQuantity()).append(" shares<br><br>");
		text.append(new PriceTable(completePosition).toHtmlSingleLine());
		return text.toString();
	}

	private String getTransactionsContent()
	{
		String text = "";
		List<Transaction> shareTransactions = completePosition.getTransactions().stream()
			.filter(t -> t.action() == Action.BUY || t.action() == Action.SELL).limit(8).toList();
		if (!shareTransactions.isEmpty())
			text = div(Map.of("style", "font-weight: bold;"), "Recent Transactions") + new TransactionsTable(shareTransactions).toHtmlSingleLine();
		return text.toString();
	}

	private String getOrdersContent()
	{
		String text = "";
		List<OpenOrder> shareOrders = completePosition.getOpenOrders().stream().filter(o -> o.option() == null)
			.sorted((a, b) -> Double.compare(a.price(), b.price())).toList();
		if (!shareOrders.isEmpty())
			text = div(Map.of("style", "font-weight: bold;"), "Open Orders") + new OrdersTable(shareOrders).toHtmlSingleLine();
		return text.toString();
	}

	private static String removeAfter(String s, String text)
	{
		int index = s.indexOf(text);
		return index != -1 ? s.substring(0, index + text.length()) : s;
	}

	private static class PriceTable extends Table
	{
		public PriceTable(CompletePosition completePosition)
		{
			super(getColumns(), getRows(completePosition));
			setShowHeader(false);
		}

		private static List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>(4);
			columns.add(new Column("", "%s", Align.L));
			columns.add(new Column("Price", "%.2f", Align.R));
			columns.add(new Column("Value", "$%.2f", Align.R));
			columns.add(new Column("G/L", "%s", Align.R));
			return columns;
		}

		private static List<List<Object>> getRows(CompletePosition completePosition)
		{
			List<List<Object>> rows = new ArrayList<>(2);
			Position position = completePosition.getPosition();
			String gainLoss = HtmlUtils.formatPercentChange(position.getGainLossPct(), true);
			rows.add(List.of("Current:", position.getPrice(), position.getMarketValue(), gainLoss));
			rows.add(List.of("Cost:", position.getCostPerShare(), position.getCostBasis()));
			return rows;
		}
	}

	private static class TransactionsTable extends Table
	{
		public TransactionsTable(Collection<? extends Transaction> transactions)
		{
			super(getColumns(), getRows(transactions));
			setShowHeader(false);
		}

		private static List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>(4);
			columns.add(new Column("Date", "%s", Align.L));
			columns.add(new Column("Action", "%s", Align.L));
			columns.add(new Column("Quantity", "%d", Align.R));
			columns.add(new Column("Price", "%.2f", Align.R));
			return columns;
		}

		private static List<List<Object>> getRows(Collection<? extends Transaction> transactions)
		{
			List<List<Object>> rows = new ArrayList<>(transactions.size());
			for (Transaction transaction : transactions)
			{
				List<Object> row = new ArrayList<>(4);
				row.add(transaction.date().toString());
				row.add(transaction.action() != null ? transaction.action().toString() : "");
				row.add(transaction.quantity());
				row.add(transaction.price());
				rows.add(row);
			}
			return rows;
		}
	}

	private static class OrdersTable extends Table
	{
		public OrdersTable(Collection<? extends OpenOrder> openOrders)
		{
			super(getColumns(), getRows(openOrders));
			setShowHeader(false);
		}

		private static List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>(3);
			columns.add(new Column("Action", "%s", Align.L));
			columns.add(new Column("Quantity", "%d", Align.R));
			columns.add(new Column("Price", "%.2f", Align.R));
			return columns;
		}

		private static List<List<Object>> getRows(Collection<? extends OpenOrder> openOrders)
		{
			List<List<Object>> rows = new ArrayList<>(openOrders.size());
			for (OpenOrder openOrder : openOrders)
			{
				List<Object> row = new ArrayList<>(3);
				row.add(openOrder.getAction());
				row.add(Math.abs(openOrder.quantity()));
				row.add(openOrder.price());
				rows.add(row);
			}
			return rows;
		}
	}
}
