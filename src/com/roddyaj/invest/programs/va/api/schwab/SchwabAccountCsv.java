package com.roddyaj.invest.programs.va.api.schwab;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.roddyaj.invest.programs.va.model.Account;
import com.roddyaj.invest.programs.va.model.Position;
import com.roddyaj.invest.util.StringUtils;

public class SchwabAccountCsv
{
	public static Account parse(Path file) throws IOException
	{
		Account account = new Account();

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
				else if (symbolIndex != -1)
				{
					String symbol = record.get(symbolIndex);
					Map<String, String> symbolMap = new HashMap<>();
					for (int i = 0; i < record.size(); i++)
					{
						String column = header.get(i);
						String value = record.get(i);
						symbolMap.put(column, value);
//						System.out.println(symbol + "|" + column + "|" + value);
					}

					Position position = new Position(symbol);
					position.setMarketValue(StringUtils.parsePrice(symbolMap.get("Market Value")));
					position.setPrice(StringUtils.parsePrice(symbolMap.get("Price")));
					position.setValues(symbolMap);
					account.addPosition(symbol, position);
				}
			}
		}

		Position accountTotal = account.getPosition("Account Total");
		if (accountTotal != null)
			account.setTotalValue(StringUtils.parsePrice(accountTotal.getValue("Market Value")));

		final Pattern datePattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
		Matcher matcher = datePattern.matcher(file.getFileName().toString());
		if (matcher.find())
			account.date = LocalDate.parse(matcher.group(1));

		return account;
	}
}
