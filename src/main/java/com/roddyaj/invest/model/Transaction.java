package com.roddyaj.invest.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.roddyaj.invest.html.HtmlUtils;
import com.roddyaj.invest.html.Table;
import com.roddyaj.invest.util.StringUtils;

public record Transaction(LocalDate date, Action action, String symbol, int quantity, double price, double amount, Option option)
{
	public boolean isOption()
	{
		return option != null;
	}

	public boolean isCallOption()
	{
		return isOption() && option.getType() == 'C';
	}

	public boolean isPutOption()
	{
		return isOption() && option.getType() == 'P';
	}

	@Override
	public String toString()
	{
		String actionText = action != null ? StringUtils.limit(action.toString(), 14) : "";
		String text = String.format("%s %-14s %-5s %3d %6.2f %8.2f", date, actionText, symbol, quantity, price, amount);
		if (option != null)
			text += (" " + option.toString());
		return text;
	}

	public static String createCostPopup(Position position, Account account)
	{
		String cost = String.format("%.2f", position.getCostPerShare());
		List<Transaction> transactions = account.getTransactions().stream()
			.filter(t -> !t.isOption() && t.symbol().equals(position.getSymbol()) && (t.action() == Action.BUY || t.action() == Action.SELL)).limit(8)
			.toList();
		if (!transactions.isEmpty())
		{
			String text = HtmlUtils.tag("div", Map.of("style", "text-decoration: underline;"), cost);
			String popupContent = new Transaction.PopupTable(transactions).toHtmlSingleLine();
			cost = HtmlUtils.createPopup(text, popupContent, true);
		}
		return cost;
	}

	public static class PopupTable extends Table
	{
		public PopupTable(Collection<? extends Transaction> transactions)
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
			for (Transaction t : transactions)
			{
				List<Object> row = new ArrayList<>(4);
				row.add(t.date.toString());
				row.add(t.action != null ? t.action.toString() : "");
				row.add(t.quantity);
				row.add(t.price);
				rows.add(row);
			}
			return rows;
		}
	}
}
