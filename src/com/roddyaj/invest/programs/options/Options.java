package com.roddyaj.invest.programs.options;

import com.roddyaj.invest.framework.Program;
import com.roddyaj.invest.model.Account;
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

		Account account = new Account(accountName);

		OptionsOutput output = new OptionsCore().run(account);

		AppFileUtils.showHtml(output.toString(), "options.html");
	}
}