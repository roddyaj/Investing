package com.roddyaj.invest.programs.options;

import java.util.List;
import java.util.stream.Collectors;

import com.roddyaj.invest.framework.Program;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.Transaction;
import com.roddyaj.invest.util.FileUtils;

public class Options implements Program
{
	public static void main(String[] args)
	{
		new Options().run(null);
	}

	@Override
	public void run(String[] args)
	{
		List<Transaction> transactions = FileUtils.readCsv("Adam_Investing_Transactions").stream().filter(r -> r.getRecordNumber() > 2)
				.map(Transaction::new).collect(Collectors.toList());
		List<Position> positions = FileUtils.readCsv("Adam_Investing-Positions").stream().filter(r -> r.getRecordNumber() > 2).map(Position::new)
				.collect(Collectors.toList());
		new OptionsCore().run(positions, transactions);
	}
}