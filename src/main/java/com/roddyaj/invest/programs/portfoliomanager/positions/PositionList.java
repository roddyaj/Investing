package com.roddyaj.invest.programs.portfoliomanager.positions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.roddyaj.invest.api.yahoo.YahooUtils;
import com.roddyaj.invest.html.Block;
import com.roddyaj.invest.html.DataFormatter;
import com.roddyaj.invest.html.HtmlUtils;
import com.roddyaj.invest.html.Table.Align;
import com.roddyaj.invest.html.Table.Column;
import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.model.CompletePosition;
import com.roddyaj.invest.model.PositionPopup;

public class PositionList
{
	public static List<Block> getBlocks(Account account)
	{
		List<Block> blocks = new ArrayList<>();

		List<CompletePosition> positions = account.getCompletePositions().stream()
			.filter(p -> !p.getPosition().isOption() && !p.getSymbol().contains("Total")).toList();

		List<CompletePosition> cashPositions = positions.stream().filter(p -> p.getSymbol().contains("Cash")).toList();
		blocks.add(new CashPositionFormatter(cashPositions, account).toBlock());

		List<CompletePosition> managedPositions = positions.stream().filter(p -> account.getAllocation(p.getSymbol()) != 0)
			.sorted((p1, p2) -> Double.compare(account.getAllocation(p2.getSymbol()), account.getAllocation(p1.getSymbol()))).toList();
		blocks.add(new ManagedPositionFormatter(managedPositions, account).toBlock());

		List<CompletePosition> unmanagedPositions = positions.stream()
			.filter(p -> account.getAllocation(p.getSymbol()) == 0 && !p.getSymbol().contains("Cash"))
			.sorted((p1, p2) -> Double.compare(p2.getPosition().getMarketValue(), p1.getPosition().getMarketValue())).toList();
		blocks.add(new UnmanagedPositionFormatter(unmanagedPositions, account).toBlock());

//		double totalPercent = positions.stream().mapToDouble(p -> p.getMarketValue() / account.getTotalValue()).sum();
//		System.out.println(totalPercent);

		return blocks;
	}

	private static class ManagedPositionFormatter extends DataFormatter<CompletePosition>
	{
		private final Account account;

		public ManagedPositionFormatter(Collection<? extends CompletePosition> positions, Account account)
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
			columns.add(new Column("G/L", "%s", Align.R));
			return columns;
		}

		@Override
		protected List<Object> toRow(CompletePosition p)
		{
			String link = new PositionPopup(p).createPopup(YahooUtils.getLink(p.getSymbol()));
			double targetPercent = account.getAllocation(p.getSymbol()) * 100;
			double percentOfAccount = p.getPosition().getMarketValue() / account.getTotalValue() * 100;
			double ratio = Math.min(percentOfAccount / targetPercent * 100, 999);
			String gainLossPctColored = HtmlUtils.formatPercentChange(p.getPosition().getGainLossPct());
			return List.of(link, targetPercent, percentOfAccount, ratio, gainLossPctColored);
		}
	}

	private static class UnmanagedPositionFormatter extends DataFormatter<CompletePosition>
	{
		private final Account account;

		public UnmanagedPositionFormatter(Collection<? extends CompletePosition> positions, Account account)
		{
			super("Unmanaged Positions", null, positions);
			this.account = account;
		}

		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Ticker", "%s", Align.L));
			columns.add(new Column("#", "%d", Align.R));
			columns.add(new Column("Percent", "%.2f%%", Align.R));
			columns.add(new Column("G/L", "%s", Align.R));
			return columns;
		}

		@Override
		protected List<Object> toRow(CompletePosition p)
		{
			String link = new PositionPopup(p).createPopup(YahooUtils.getLink(p.getSymbol()));
			double percentOfAccount = p.getPosition().getMarketValue() / account.getTotalValue() * 100;
			String gainLossPctColored = HtmlUtils.formatPercentChange(p.getPosition().getGainLossPct());
			return List.of(link, p.getPosition().getQuantity(), percentOfAccount, gainLossPctColored);
		}
	}

	private static class CashPositionFormatter extends DataFormatter<CompletePosition>
	{
		private final Account account;

		public CashPositionFormatter(Collection<? extends CompletePosition> positions, Account account)
		{
			super("Cash", null, positions);
			this.account = account;
		}

		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Amount", "$%,.2f", Align.R));
			columns.add(new Column("Target", "%.2f%%", Align.R));
			columns.add(new Column("Actual", "%.2f%%", Align.R));
			return columns;
		}

		@Override
		protected List<Object> toRow(CompletePosition p)
		{
			double targetPercent = account.getAllocation("cash") * 100;
			double percentOfAccount = p.getPosition().getMarketValue() / account.getTotalValue() * 100;
			return List.of(p.getPosition().getMarketValue(), targetPercent, percentOfAccount);
		}
	}
}