package com.roddyaj.invest.model;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import com.roddyaj.invest.util.AppFileUtils;
import com.roddyaj.invest.util.AppFileUtils.FileType;
import com.roddyaj.invest.util.FileUtils;

public class Account
{
	private final String name;
	private List<Position> positions;
	private List<Transaction> transactions;

	public Account(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public List<Position> getPositions()
	{
		if (positions == null)
		{
			Path positionsFile = AppFileUtils.getAccountFile(name, FileType.POSITIONS);
			positions = positionsFile != null
					? FileUtils.readCsv(positionsFile).stream().filter(r -> r.getRecordNumber() > 2).map(Position::new).collect(Collectors.toList())
					: List.of();
		}
		return positions;
	}

	public List<Transaction> getTransactions()
	{
		if (transactions == null)
		{
			Path transactionsFile = AppFileUtils.getAccountFile(name, FileType.TRANSACTIONS);
			transactions = transactionsFile != null ? FileUtils.readCsv(transactionsFile).stream().filter(r -> r.getRecordNumber() > 2)
					.map(Transaction::new).collect(Collectors.toList()) : List.of();
		}
		return transactions;
	}
}
