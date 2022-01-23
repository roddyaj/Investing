package com.roddyaj.invest.programs.portfoliomanager;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import com.roddyaj.invest.model.Output;
import com.roddyaj.invest.programs.portfoliomanager.options.OptionsCore;
import com.roddyaj.invest.programs.portfoliomanager.options.OptionsOutput;
import com.roddyaj.invest.programs.portfoliomanager.positions.IncomeCalculator;
import com.roddyaj.invest.programs.portfoliomanager.positions.IncomeCalculator.MonthlyIncome;
import com.roddyaj.invest.programs.portfoliomanager.positions.IncomeCalculator.MonthlyIncomeFormatter;
import com.roddyaj.invest.programs.portfoliomanager.positions.OddLots;
import com.roddyaj.invest.programs.portfoliomanager.positions.OddLotsOutput;
import com.roddyaj.invest.programs.portfoliomanager.positions.PositionList;
import com.roddyaj.invest.programs.portfoliomanager.positions.PositionManager;
import com.roddyaj.invest.programs.portfoliomanager.positions.PositionManagerOutput;
import com.roddyaj.invest.programs.portfoliomanager.positions.ReturnCalculator;
import com.roddyaj.invest.util.AppFileUtils;
import com.roddyaj.invest.util.Args;
import com.roddyaj.invest.util.CollectionUtils;

public class PortfolioManager implements Program
{
	@Override
	public void run(Queue<String> args)
	{
		boolean offline = Args.isPresent(args, "-o");
		String accountName = args.poll();

		Input input = new Input(accountName, offline);
		Account account = input.getAccount();

		// Run everything
		PositionManagerOutput positionsOutput = new PositionManager(input).run();
		OddLotsOutput oddLotsOutput = new OddLots(account).run();
		OptionsOutput optionsOutput = new OptionsCore(input).run();
		double portfolioReturn = new ReturnCalculator(account, account.getAccountSettings()).run();
		List<MonthlyIncome> monthlyIncome = new IncomeCalculator(account).run();

		List<String> lines = new ArrayList<>();
		lines.addAll(getMessages(account, positionsOutput, optionsOutput).toHtml());
		lines.addAll(getMainHeader(account).toHtml());

		Block summary = getSummary(account, portfolioReturn);

		// Main content blocks
		List<Column> columns = new ArrayList<>();
		columns.add(new Column(CollectionUtils.join(positionsOutput.getBlocks(), oddLotsOutput.getBlock())));
		List<Block> optionsBlocks = new ArrayList<>(optionsOutput.getActionsBlocks(account));
		optionsBlocks.add(optionsOutput.getCurrentOptionsBlock());
		columns.add(new Column(optionsBlocks));
		List<Block> positions = PositionList.getBlocks(input.getAccount());
		columns.add(new Column(positions.get(1)));
		columns.add(new Column(positions.get(2)));
		columns.add(new Column(List.of(positions.get(0), summary, new MonthlyIncomeFormatter(monthlyIncome).toBlock())));
		lines.addAll(new Row(columns).toHtml());

		String html = HtmlUtils.toDocument(account.getName().replace('_', ' '), lines);

		AppFileUtils.showHtml(html, account.getName() + ".html");
	}

	private static Block getMessages(Account account, Output... output)
	{
		List<Message> messages = new ArrayList<>();
		messages.addAll(account.getMessages());
		for (Output anOutput : output)
			messages.addAll(anOutput.getMessages());
		return Message.toBlock(messages);
	}

	private static Block getMainHeader(Account account)
	{
		String delimiter = HtmlUtils.color(" | ", "var(--line-color)");
		String links = String.join(delimiter, SchwabDataSource.getNavigationLinks());
		String title = account.getName().replace('_', ' ');

		List<String> lines = new ArrayList<>();
		lines.add(HtmlUtils.startTag("div", Map.of("style", "display: flex;")));
		lines.add(HtmlUtils.tag("div", Map.of("class", "main-header-item"), links));
		lines.add(HtmlUtils.tag("div", Map.of("class", "main-header-item title", "style", "text-align: center;"), title));
		lines.add(HtmlUtils.tag("div", Map.of("class", "main-header-item", "style", "text-align: right;"),
				account.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
		lines.add(HtmlUtils.endTag("div"));
		return new Block(null, null, new RawHtml(lines));
	}

	private static Block getSummary(Account account, double portfolioReturn)
	{
		KeyValueData summary = new KeyValueData("Summary");
		summary.addData("Balance:", String.format("$%.0f", account.getTotalValue()));
		summary.addData("Return:", String.format("%.2f%%", portfolioReturn * 100));
		return summary.toBlock();
	}
}