package com.roddyaj.invest.programs.portfoliomanager;

import java.util.ArrayList;
import java.util.List;

import com.roddyaj.invest.framework.Program;
import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.model.Input;
import com.roddyaj.invest.model.Message;
import com.roddyaj.invest.programs.portfoliomanager.options.OptionsCore;
import com.roddyaj.invest.programs.portfoliomanager.options.OptionsOutput;
import com.roddyaj.invest.programs.portfoliomanager.positions.PositionManagerCore;
import com.roddyaj.invest.programs.portfoliomanager.positions.PositionManagerOutput;
import com.roddyaj.invest.util.AppFileUtils;
import com.roddyaj.invest.util.HtmlFormatter;

public class PortfolioManager implements Program
{
	@Override
	public void run(String... args)
	{
		String accountName = args[0];
		Input input = new Input(accountName);

		PositionManagerOutput positionsOutput = new PositionManagerCore(input).run();
		OptionsOutput optionsOutput = new OptionsCore(input).run();

		showHtml(input.getAccount(), positionsOutput, optionsOutput);
	}

	private void showHtml(Account account, PositionManagerOutput positionsOutput, OptionsOutput optionsOutput)
	{
		List<String> lines = new ArrayList<>();

		// Messages block
		List<Message> messages = new ArrayList<>();
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