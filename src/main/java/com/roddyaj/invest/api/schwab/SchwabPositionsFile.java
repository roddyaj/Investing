package com.roddyaj.invest.api.schwab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.roddyaj.invest.util.FileUtils;

public class SchwabPositionsFile
{
	private static final Pattern DATE_PATTERN = Pattern.compile("as of (.+?)\"");
	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a v, MM/dd/yyy");

	private final Path file;

	private final ZonedDateTime time;

	private List<SchwabPosition> positions;

	public SchwabPositionsFile(Path file)
	{
		this.file = file;
		this.time = getTime(file);
	}

	public ZonedDateTime getTime()
	{
		return time;
	}

	public List<SchwabPosition> getPositions()
	{
		if (positions == null)
			positions = FileUtils.readCsv(file, 2).stream().map(SchwabPosition::new).toList();
		return positions;
	}

	public String toCsvString()
	{
		return getPositions().stream().map(SchwabPosition::toCsvString).collect(Collectors.joining("\n"));
	}

	public static ZonedDateTime getTime(Path file)
	{
		ZonedDateTime time = null;
		try
		{
			List<String> lines = Files.readAllLines(file);

			Matcher matcher = DATE_PATTERN.matcher(lines.get(0));
			if (matcher.find())
				time = ZonedDateTime.parse(matcher.group(1), DATE_TIME_FORMAT);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return time;
	}
}
