package com.roddyaj.invest.programs.portfoliomanager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.roddyaj.invest.api.schwab.SchwabDataSource;
import com.roddyaj.invest.framework.Program;
import com.roddyaj.invest.html.Block;
import com.roddyaj.invest.html.Column;
import com.roddyaj.invest.html.HtmlUtils;
import com.roddyaj.invest.html.RawHtml;
import com.roddyaj.invest.html.Row;
import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.model.Input;
import com.roddyaj.invest.model.Message;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.programs.portfoliomanager.options.OptionsCore;
import com.roddyaj.invest.programs.portfoliomanager.options.OptionsOutput;
import com.roddyaj.invest.programs.portfoliomanager.positions.OddLots;
import com.roddyaj.invest.programs.portfoliomanager.positions.OddLotsOutput;
import com.roddyaj.invest.programs.portfoliomanager.positions.PositionList;
import com.roddyaj.invest.programs.portfoliomanager.positions.PositionManager;
import com.roddyaj.invest.programs.portfoliomanager.positions.PositionManagerOutput;
import com.roddyaj.invest.programs.portfoliomanager.positions.ReturnCalculator;
import com.roddyaj.invest.programs.portfoliomanager.positions.UnmanagedPositions;
import com.roddyaj.invest.util.AppFileUtils;
import com.roddyaj.invest.util.CollectionUtils;

public class PortfolioManager implements Program
{
	@Override
	public void run(Queue<String> args)
	{
		boolean offline = isOffline(args);
		String accountName = args.poll();

		Input input = new Input(accountName, offline);
		Account account = input.getAccount();

		// Run everything
		PositionManagerOutput positionsOutput = new PositionManager(input).run();
		OddLotsOutput oddLotsOutput = new OddLots(account).run();
		List<Position> unmanagedPositions = new UnmanagedPositions(account).run();
		OptionsOutput optionsOutput = new OptionsCore(input).run();
		double portfolioReturn = new ReturnCalculator(account, account.getAccountSettings()).run();

		KeyValueData summary = new KeyValueData("Summary");
		summary.addData("Balance:", String.format("$%.0f", account.getTotalValue()));
		summary.addData("Return:", String.format("%.2f%%", portfolioReturn * 100));
		if (!unmanagedPositions.isEmpty())
			summary.addData("Untracked Excess:", String.format("$%.0f", unmanagedPositions.get(0).getMarketValue()));

		List<String> lines = new ArrayList<>();

		// Messages block
		List<Message> messages = new ArrayList<>();
		messages.addAll(account.getMessages());
		messages.addAll(positionsOutput.getMessages());
		messages.addAll(optionsOutput.getMessages());
		lines.addAll(Message.toHtml(messages));

		// Links block
		lines.addAll(getMainHeader(account));

		// Main content blocks
		List<Column> columns = new ArrayList<>();
//		columns.add(new Column(new Position.StockHtmlFormatter("Untracked", null, unmanagedPositions).toBlock()));
		columns.add(new Column(CollectionUtils.join(positionsOutput.getBlocks(), oddLotsOutput.getBlock())));
		columns.add(new Column(optionsOutput.getActionsBlocks(account)));
		columns.add(new Column(optionsOutput.getCurrentOptionsBlock()));
		columns.add(new Column(PositionList.getBlocks(input.getAccount())));
		columns.add(new Column(List.of(summary.toBlock(), optionsOutput.getIncomeBlock())));
		lines.addAll(new Row(columns).toHtml());

		String html = HtmlUtils.toDocument(account.getName().replace('_', ' '), lines);

		AppFileUtils.showHtml(html, account.getName() + ".html");
	}

	private static boolean isOffline(Queue<String> args)
	{
		boolean offline = false;
		for (Iterator<String> iter = args.iterator(); iter.hasNext();)
		{
			String arg = iter.next();
			if (arg.equals("-o"))
			{
				offline = true;
				iter.remove();
			}
		}
		return offline;
	}

	private List<String> getMainHeader(Account account)
	{
		String links = String.join(" | ", SchwabDataSource.getNavigationLinks());
		String title = account.getName().replace('_', ' ');

		List<String> lines = new ArrayList<>();
		lines.add(HtmlUtils.startTag("div", Map.of("style", "display: flex;")));
		lines.add(HtmlUtils.tag("div", Map.of("class", "main-header-item"), links));
		lines.add(HtmlUtils.tag("div", Map.of("class", "main-header-item title", "style", "text-align: center;"), title));
		lines.add(HtmlUtils.tag("div", Map.of("class", "main-header-item", "style", "text-align: right;"), account.getDate().toString()));
		lines.add(HtmlUtils.endTag("div"));
		return new Block(null, null, new RawHtml(lines)).toHtml();
	}
}