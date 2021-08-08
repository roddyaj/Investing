package com.roddyaj.invest.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public final class FileUtils
{
	// TODO abstract this out to AppFileUtils
	private static final Path DEFAULT_DIR = Paths.get(System.getProperty("user.home"), "Downloads");

	public static List<String> readLines(String file)
	{
		try
		{
			Path path = getPath(file);
			return path != null ? Files.readAllLines(path) : List.of();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return List.of();
	}

	public static List<CSVRecord> readCsv(Path path, int headerLine)
	{
		List<CSVRecord> records = new ArrayList<>();
		try
		{
			List<String> lines = Files.readAllLines(path);
			String content = lines.subList(headerLine, lines.size()).stream().collect(Collectors.joining("\n"));

			CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader().withAllowMissingColumnNames();
			try (CSVParser parser = CSVParser.parse(content, format))
			{
				for (CSVRecord record : parser)
					records.add(record);

//				// Output code for header constants
//				for (String header : parser.getHeaderNames())
//				{
//					String variable = header.toUpperCase().replace(' ', '_').replace('/', '_').replace('-', '_').replace("?", "").replace("&", "AND")
//							.replace("%", "PERCENT");
//					if (!variable.isEmpty())
//					{
//						if (Character.isDigit(variable.charAt(0)))
//							variable = "_" + variable;
//						System.out.println("private static final String " + variable + " = \"" + header + "\";");
//					}
//				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return records;
	}

	public static void writeLines(String file, Iterable<? extends CharSequence> lines)
	{
		try
		{
			Path path = Paths.get(DEFAULT_DIR.toString(), file);
			Files.write(path, lines);
			System.out.println("Wrote to " + path);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	// TODO abstract this out to AppFileUtils
	public static Path getPath(String file)
	{
		Path actualPath = null;
		try
		{
			Path argPath = Paths.get(file);
			if (Files.exists(argPath))
				actualPath = argPath;
			else if (Files.exists(DEFAULT_DIR))
				actualPath = Files.list(DEFAULT_DIR).filter(p -> p.getFileName().toString().startsWith(file))
						.sorted((o1, o2) -> o2.getFileName().compareTo(o1.getFileName())).findFirst().orElse(null);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return actualPath;
	}

	public static Stream<Path> list(Path dir, String pattern)
	{
		if (Files.exists(dir))
		{
			try
			{
				return Files.list(dir).filter(p -> p.getFileName().toString().matches(pattern));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return Stream.empty();
	}
}
