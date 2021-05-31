package com.roddyaj.invest.programs.options;

import com.roddyaj.invest.framework.Program;
import com.roddyaj.invest.model.Input;
import com.roddyaj.invest.util.AppFileUtils;

public class Options implements Program
{
	// For running in IDE
	public static void main(String[] args)
	{
		new Options().run("PCRA");
	}

	@Override
	public void run(String... args)
	{
		String accountName = args[0];

		Input input = new Input(accountName);

		OptionsOutput output = new OptionsCore().run(input);

		AppFileUtils.showHtml(output.toString(), "options.html");
	}
}