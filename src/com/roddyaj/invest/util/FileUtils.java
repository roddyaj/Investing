package com.roddyaj.invest.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public final class FileUtils
{
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

	public static List<CSVRecord> readCsv(String file)
	{
		List<CSVRecord> records = new ArrayList<>();
		Path path = getPath(file);
		Charset charset = Charset.forName("UTF-8");
		CSVFormat format = CSVFormat.DEFAULT;
		try
		{
			try (CSVParser parser = CSVParser.parse(path, charset, format))
			{
				for (CSVRecord record : parser)
					records.add(record);
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
}
