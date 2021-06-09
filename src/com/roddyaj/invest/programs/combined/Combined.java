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

		List<String> lines = new ArrayList<>();
		List<Message> messages = new ArrayList<Message>();
		messages.addAll(positionsOutput.getMessages());
		messages.addAll(optionsOutput.getMessages());
		lines.addAll(new Message.MessageFormatter().toBlock(messages, null));
		lines.addAll(positionsOutput.getContent());
		lines.addAll(optionsOutput.getContent());
		String html = HtmlFormatter.toDocument(input.account.getName(), lines);

		AppFileUtils.showHtml(html, input.account.getName() + ".html");
	}
}