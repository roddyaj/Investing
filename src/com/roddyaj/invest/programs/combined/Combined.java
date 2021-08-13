package com.roddyaj.invest.programs.combined;

import java.util.ArrayList;
import java.util.List;

import com.roddyaj.invest.framework.Program;
import com.roddyaj.invest.model.Input;
import com.roddyaj.invest.model.Message;
import com.roddyaj.invest.programs.options.OptionsCore;
import com.roddyaj.invest.programs.options.OptionsOutput;
import com.roddyaj.invest.programs.positions.PositionManagerCore;
import com.roddyaj.invest.programs.positions.PositionManagerOutput;
import com.roddyaj.invest.util.AppFileUtils;
import com.roddyaj.invest.util.HtmlFormatter;

public class Combined implements Program
{
	@Override
	public void run(String... args)
	{
		String accountName = args[0];
		Input input = new Input(accountName);

		PositionManagerOutput positionsOutput = new PositionManagerCore(input).run();
		OptionsOutput optionsOutput = new OptionsCore(input).run();

		showHtml(input, positionsOutput, optionsOutput);
	}

	private void showHtml(Input input, PositionManagerOutput positionsOutput, OptionsOutput optionsOutput)
	{
		List<String> lines = new ArrayList<>();

		// Messages block
		List<Message> messages = new ArrayList<Message>();
		messages.addAll(positionsOutput.getMessages());
		messages.addAll(optionsOutput.getMessages());
		lines.addAll(new Message.MessageFormatter().toBlock(messages, "Messages", null));

		// Links block
		lines.addAll(getLinks());

		// Main content blocks
		List<String> columnLines = new ArrayList<>();
		columnLines.addAll(HtmlFormatter.toColumn(positionsOutput.getContent()));
		columnLines.addAll(HtmlFormatter.toColumn(optionsOutput.getActionsHtml()));
		columnLines.addAll(HtmlFormatter.toColumn(optionsOutput.getInfoHtml()));
		lines.addAll(HtmlFormatter.toRow(columnLines));

		String html = HtmlFormatter.toDocument(input.account.getName().replace('_', ' '), lines);

		AppFileUtils.showHtml(html, input.account.getName() + ".html");
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