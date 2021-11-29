package com.roddyaj.invest.programs.portfoliomanager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import com.roddyaj.invest.framework.Program;
import com.roddyaj.invest.html.Column;
import com.roddyaj.invest.html.HtmlFormatter;
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

		KeyValueData statistics = new KeyValueData();
		statistics.addData("Return:", String.format("%.2f%%", portfolioReturn * 100));
		statistics.addData("Untracked Excess:", String.format("$%.0f", unmanagedPositions.get(0).getMarketValue()));

		List<String> lines = new ArrayList<>();

		// Messages block
		List<Message> messages = new ArrayList<>();
		messages.addAll(account.getMessages());
		messages.addAll(positionsOutput.getMessages());
		messages.addAll(optionsOutput.getMessages());
		lines.addAll(new Message.MessageFormatter(messages).toHtml());

		// Links block
		lines.addAll(getLinks());

		// Main content blocks
		List<Column> columns = new ArrayList<>();
//		columns.add(new Column(new Position.StockHtmlFormatter("Untracked", null, unmanagedPositions).toBlock()));
		columns.add(new Column(CollectionUtils.join(positionsOutput.getBlocks(), oddLotsOutput.getBlock())));
		columns.add(new Column(optionsOutput.getActionsBlocks()));
		columns.add(new Column(optionsOutput.getCurrentOptionsBlock()));
		columns.add(new Column(PositionList.getBlocks(input.getAccount())));
		columns.add(new Column(List.of(optionsOutput.getIncomeBlock(), statistics.toBlock())));
		lines.addAll(new Row(columns).toHtml());

		String html = HtmlFormatter.toDocument(account.getName().replace('_', ' '), lines);

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

	private List<String> getLinks()
	{
		List<String> links = new ArrayList<>();
		links.add(HtmlFormatter.toLink("https://client.schwab.com/Areas/Accounts/Positions", "Positions"));
		links.add("|");
		links.add(HtmlFormatter.toLink("https://client.schwab.com/Apps/accounts/transactionhistory", "History"));
		links.add("|");
		links.add(HtmlFormatter.toLink("https://client.schwab.com/Apps/Accounts/Balances", "Balances"));
		links.add("|");
		links.add(HtmlFormatter.toLink("https://client.schwab.com/Trade/OrderStatus/ViewOrderStatus.aspx?ViewTypeFilter=Open", "Open Orders"));
		links.add("|");
		links.add(HtmlFormatter.toLink("https://client.schwab.com/Trade/OrderStatus/ViewOrderStatus.aspx?ViewTypeFilter=Today", "Today's Orders"));
		return HtmlFormatter.toSimpleColumnTable(links);
	}
}