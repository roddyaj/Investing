package com.roddyaj.invest.va.api.schwab;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class SchwabAccountCsv
{
	public static Map<String, Map<String, String>> parse(Path file) throws IOException
	{
		Map<String, Map<String, String>> map = new HashMap<>();

		Charset charset = Charset.forName("UTF-8");
		CSVFormat format = CSVFormat.DEFAULT;
		try (CSVParser parser = CSVParser.parse(file, charset, format))
		{
			CSVRecord header = null;
			int symbolIndex = -1;
			for (CSVRecord record : parser)
			{
				if (record.size() <= 1)
					continue;

				if (header == null)
				{
					header = record;
					for (int i = 0; i < record.size(); i++)
					{
						String value = record.get(i);
						if ("Symbol".equals(value))
						{
							symbolIndex = i;
							break;
						}
					}
				}
				else
				{
					String symbol = record.get(symbolIndex);
					Map<String, String> symbolMap = new HashMap<>();
					for (int i = 0; i < record.size(); i++)
					{
						String column = header.get(i);
						String value = record.get(i);
						symbolMap.put(column, value);
					}
					map.put(symbol, symbolMap);
				}
			}
		}

		return map;
	}

	public static double parsePrice(String text)
	{
		return Double.parseDouble(text.replace("$", "").replace(",", ""));
	}
}
