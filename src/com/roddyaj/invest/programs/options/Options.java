package com.roddyaj.invest.programs.options;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.roddyaj.invest.framework.Program;
import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.util.AppFileUtils;
import com.roddyaj.invest.util.FileUtils;

public class Options implements Program
{
	// For testing in IDE
	public static void main(String[] args)
	{
		new Options().run("PCRA");
	}

	@Override
	public void run(String... args)
	{
		String accountName = args[0];
		accountName = AppFileUtils.getFullAccountName(accountName);
		Account account = new Account(accountName);

		// Run the algorithm
		OptionsOutput output = new OptionsCore().run(account);

		// Write/display HTML output
		Path path = Paths.get(FileUtils.DEFAULT_DIR.toString(), "options.html");
		try
		{
			Files.writeString(path, output.toString());
			Desktop.getDesktop().browse(path.toUri());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}