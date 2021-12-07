package com.roddyaj.invest.programs.portfoliomanager.positions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.roddyaj.invest.api.yahoo.YahooUtils;
import com.roddyaj.invest.html.Block;
import com.roddyaj.invest.html.DataFormatter;
import com.roddyaj.invest.html.HtmlFormatter;
import com.roddyaj.invest.html.Table.Align;
import com.roddyaj.invest.html.Table.Column;
import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.model.Position;

public class PositionList
{
	public static List<Block> getBlocks(Account account)
	{
		List<Block> blocks = new ArrayList<>();

		List<Position> positions = account.getPositions().stream().filter(p -> !p.isOption() && !p.getSymbol().contains("Total")).toList();

		List<Position> cashPositions = positions.stream().filter(p -> p.getSymbol().contains("Cash")).toList();
		blocks.add(new CashPositionFormatter(cashPositions, account).toBlock());

		List<Position> managedPositions = positions.stream().filter(p -> account.getAllocation(p.getSymbol()) != 0)
				.sorted((p1, p2) -> Double.compare(account.getAllocation(p2.getSymbol()), account.getAllocation(p1.getSymbol()))).toList();
		blocks.add(new ManagedPositionFormatter(managedPositions, account).toBlock());

		List<Position> unmanagedPositions = positions.stream()
				.filter(p -> account.getAllocation(p.getSymbol()) == 0 && !p.getSymbol().contains("Cash"))
				.sorted((p1, p2) -> Double.compare(p2.getMarketValue(), p1.getMarketValue())).toList();
		blocks.add(new UnmanagedPositionFormatter(unmanagedPositions, account).toBlock());

//		double totalPercent = positions.stream().mapToDouble(p -> p.getMarketValue() / account.getTotalValue()).sum();
//		System.out.println(totalPercent);

		return blocks;
	}

	private static class ManagedPositionFormatter extends DataFormatter<Position>
	{
		private final Account account;

		public ManagedPositionFormatter(Collection<? extends Position> positions, Account account)
		{
			super("Managed Positions", null, positions);
			this.account = account;
		}

		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Ticker", "%s", Align.L));
			columns.add(new Column("Target", "%.2f%%", Align.R));
			columns.add(new Column("Actual", "%.2f%%", Align.R));
			columns.add(new Column("Ratio", "%.0f%%", Align.R));
			return columns;
		}

		@Override
		protected List<Object> toRow(Position p)
		{
			double targetPercent = account.getAllocation(p.getSymbol()) * 100;
			double percentOfAccount = p.getMarketValue() / account.getTotalValue() * 100;
			double ratio = Math.min(percentOfAccount / targetPercent * 100, 999);
			return List.of(YahooUtils.getLink(p.getSymbol()), targetPercent, percentOfAccount, ratio);
		}
	}

	private static class UnmanagedPositionFormatter extends DataFormatter<Position>
	{
		private final Account account;

		public UnmanagedPositionFormatter(Collection<? extends Position> positions, Account account)
		{
			super("Unmanaged Positions", null, positions);
			this.account = account;
		}

		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Ticker", "%s", Align.L));
			columns.add(new Column("Percent", "%.2f%%", Align.R));
			columns.add(new Column("Total", "%s", Align.R));
			return columns;
		}

		@Override
		protected List<Object> toRow(Position p)
		{
			double percentOfAccount = p.getMarketValue() / account.getTotalValue() * 100;
			String gainLossPctColored = HtmlFormatter.formatPercentChange(p.getGainLossPct());
			return List.of(YahooUtils.getLink(p.getSymbol()), percentOfAccount, gainLossPctColored);
		}
	}

	private static class CashPositionFormatter extends DataFormatter<Position>
	{
		private final Account account;

		public CashPositionFormatter(Collection<? extends Position> positions, Account account)
		{
			super("Cash", null, positions);
			this.account = account;
		}

		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Amount", "$%.2f", Align.R));
			columns.add(new Column("Target", "%.2f%%", Align.R));
			columns.add(new Column("Actual", "%.2f%%", Align.R));
			return columns;
		}

		@Override
		protected List<Object> toRow(Position p)
		{
			double targetPercent = account.getAllocation("cash") * 100;
			double percentOfAccount = p.getMarketValue() / account.getTotalValue() * 100;
			return List.of(p.getMarketValue(), targetPercent, percentOfAccount);
		}
	}
}