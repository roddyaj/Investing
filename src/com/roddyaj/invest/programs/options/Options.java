package com.roddyaj.invest.programs.options;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.roddyaj.invest.framework.Program;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.Transaction;
import com.roddyaj.invest.util.AppFileUtils;
import com.roddyaj.invest.util.AppFileUtils.FileType;
import com.roddyaj.invest.util.FileUtils;

public class Options implements Program
{
	// For testing in IDE
	public static void main(String[] args)
	{
		new Options().run("PCRA_Trust");
	}

	@Override
	public void run(String... args)
	{
		String account = args[0];
		Path positionsFile = AppFileUtils.getAccountFile(account, FileType.POSITIONS);
		Path transactionsFile = AppFileUtils.getAccountFile(account, FileType.TRANSACTIONS);

		// Read input files
		List<Transaction> transactions = transactionsFile != null
				? FileUtils.readCsv(transactionsFile).stream().filter(r -> r.getRecordNumber() > 2).map(Transaction::new).collect(Collectors.toList())
				: List.of();
		List<Position> positions = positionsFile != null
				? FileUtils.readCsv(positionsFile).stream().filter(r -> r.getRecordNumber() > 2).map(Position::new).collect(Collectors.toList())
				: List.of();

		// Run the algorithm
		OptionsOutput output = new OptionsCore().run(account, positions, transactions);

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