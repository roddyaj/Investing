package com.roddyaj.invest.programs.vf.api.schwab;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.roddyaj.invest.programs.vf.model.SymbolData;

// TODO
// - handle BOM
// - handle columns in any order
public class SchwabScreenCsv
{
	public static List<SymbolData> parseSymbols(Path file) throws IOException
	{
		List<SymbolData> symbols = new ArrayList<>();
		Charset charset = Charset.forName("UTF-8");
		CSVFormat format = CSVFormat.DEFAULT;
		boolean isHeader = true;
		try (CSVParser parser = CSVParser.parse(file, charset, format))
		{
			for (CSVRecord csvRecord : parser)
			{
				if (!isHeader)
				{
					SymbolData stock = new SymbolData(csvRecord.get(0));
//					stock.setPrice(StringUtils.parsePrice(csvRecord.get(2)));
					symbols.add(stock);
				}
				isHeader = false;
			}
		}
		return symbols;
	}
}
