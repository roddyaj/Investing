package com.roddyaj.invest.api.schwab;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.roddyaj.invest.util.FileUtils;

public class SchwabTransactionsFile
{
	private static final Pattern FILE_PATTERN = Pattern.compile("(.+?)_Transactions_([-\\d]+).CSV");

	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

	private final Path file;

	private final LocalDateTime time;

	private List<SchwabTransaction> transactions;

	public SchwabTransactionsFile(Path file)
	{
		this.file = file;
		this.time = getTime(file);
	}

	public LocalDateTime getTime()
	{
		return time;
	}

	public List<SchwabTransaction> getTransactions()
	{
		if (transactions == null)
			transactions = FileUtils.readCsv(file, 1).stream().map(SchwabTransaction::new).filter(t -> t.date() != null).toList();
		return transactions;
	}

	public static LocalDateTime getTime(Path file)
	{
		LocalDateTime time = null;
		Matcher m = FILE_PATTERN.matcher(file.getFileName().toString());
		if (m.find())
			time = LocalDateTime.parse(m.group(2), DATE_TIME_FORMAT);
		return time;
	}
}
