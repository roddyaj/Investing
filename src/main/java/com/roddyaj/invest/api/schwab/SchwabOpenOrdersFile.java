package com.roddyaj.invest.api.schwab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import com.roddyaj.invest.util.FileUtils;

public class SchwabOpenOrdersFile
{
	private final Path file;

	private final LocalDateTime time;

	private List<SchwabOpenOrder> openOrders;

	public SchwabOpenOrdersFile(Path file)
	{
		this.file = file;
		this.time = getTime(file);
	}

	public LocalDateTime getTime()
	{
		return time;
	}

	public List<SchwabOpenOrder> getOpenOrders()
	{
		if (openOrders == null)
		{
			try
			{
				// Correct the file contents to be valid CSV
				List<String> lines = Files.lines(file).filter(line -> !line.isEmpty()).map(line -> line.replace("\" Shares", " Shares\"")
					.replace("\" Share", " Share\"").replace("\" Contracts", " Contracts\"").replace("\" Contract", " Contract\"")).toList();

				openOrders = FileUtils.readCsv(lines).stream().map(SchwabOpenOrder::new).toList();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return openOrders;
	}

	public String toCsvString()
	{
		return getOpenOrders().stream().map(SchwabOpenOrder::toCsvString).collect(Collectors.joining("\n"));
	}

	public static LocalDateTime getTime(Path file)
	{
		LocalDateTime time = null;
		try
		{
			time = LocalDateTime.ofInstant(Files.getLastModifiedTime(file).toInstant(), ZoneId.systemDefault());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return time;
	}
}
