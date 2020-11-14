package com.roddyaj.vf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.roddyaj.vf.api.schwab.csv.SchwabScreenCsv;

public final class Main
{
	public static void main(String[] args)
	{
		Path file = args.length > 0 ? Paths.get(args[0]) : null;
		if (file == null)
		{
			System.out.println("Error: No file specified");
			return;
		}
		if (!Files.exists(file))
		{
			System.out.println(String.format("Error: %s does not exist", file.toString()));
			return;
		}

		try
		{
			List<String> symbols = SchwabScreenCsv.parseSymbols(file);
			for (String symbol : symbols)
				System.out.println(symbol);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
