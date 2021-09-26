package com.roddyaj.invest.programs.portfoliomanager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import com.roddyaj.invest.framework.Program;
import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.model.Input;
import com.roddyaj.invest.model.Message;
import com.roddyaj.invest.programs.portfoliomanager.options.OptionsCore;
import com.roddyaj.invest.programs.portfoliomanager.options.OptionsOutput;
import com.roddyaj.invest.programs.portfoliomanager.positions.PositionManager;
import com.roddyaj.invest.programs.portfoliomanager.positions.PositionManagerOutput;
import com.roddyaj.invest.programs.portfoliomanager.positions.ReturnCalculator;
import com.roddyaj.invest.util.AppFileUtils;
import com.roddyaj.invest.util.HtmlFormatter;

public class PortfolioManager implements Program
{
	@Override
	public void run(Queue<String> args)
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
		String accountName = args.poll();

		Input input = new Input(accountName, offline);

		PositionManagerOutput positionsOutput = new PositionManager(input).run();
		OptionsOutput optionsOutput = new OptionsCore(input).run();
		double portfolioReturn = new ReturnCalculator(input.getAccount(), input.getAccount().getAccountSettings()).run();
		System.out.println(String.format("Return: %.2f%%", portfolioReturn * 100));

		showHtml(input.getAccount(), positionsOutput, optionsOutput);
	}

	private void showHtml(Account account, PositionManagerOutput positionsOutput, OptionsOutput optionsOutput)
	{
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
		List<String> columnLines = new ArrayList<>();
		columnLines.addAll(HtmlFormatter.toColumn(positionsOutput.getContent()));
		columnLines.addAll(HtmlFormatter.toColumn(optionsOutput.getActionsHtml()));
		columnLines.addAll(HtmlFormatter.toColumn(optionsOutput.getCurrentOptionsHtml()));
		columnLines.addAll(HtmlFormatter.toColumn(optionsOutput.getIncomeHtml()));
		lines.addAll(HtmlFormatter.toRow(columnLines));

		String html = HtmlFormatter.toDocument(account.getName().replace('_', ' '), lines);

		AppFileUtils.showHtml(html, account.getName() + ".html");
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