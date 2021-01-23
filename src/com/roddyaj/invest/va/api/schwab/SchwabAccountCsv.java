package com.roddyaj.invest.va.api.schwab;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.roddyaj.invest.va.model.Account;
import com.roddyaj.invest.va.model.Position;

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

					Position position = new Position();
					position.setMarketValue(parsePrice(symbolMap.get("Market Value")));
					position.setPrice(parsePrice(symbolMap.get("Price")));
					position.setValues(symbolMap);
					account.addPosition(symbol, position);
				}
			}
		}

		Position accountTotal = account.getPosition("Account Total");
		if (accountTotal != null)
			account.setTotalValue(parsePrice(accountTotal.getValue("Market Value")));

		return account;
	}

	public static double parsePrice(String text)
	{
		if ("--".equals(text))
			return 0;
		return Double.parseDouble(text.replace("$", "").replace(",", ""));
	}
}
