package com.roddyaj.invest.programs.va2;

import com.roddyaj.invest.framework.Program;
import com.roddyaj.invest.model.Input;
import com.roddyaj.invest.util.AppFileUtils;

public class PositionManager implements Program
{
	@Override
	public void run(String... args)
	{
		String accountName = args[0];

		Input input = new Input(accountName);

		PositionManagerOutput output = new PositionManagerCore(input).run();

		AppFileUtils.showHtml(output.toString(), "positions.html");
	}
}