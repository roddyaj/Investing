package com.roddyaj.vf.api.schwab;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

// TODO
// - handle BOM
// - handle columns in any order
public class SchwabScreenCsv
{
	public static List<String> parseSymbols(Path file) throws IOException
	{
		List<String> symbols = new ArrayList<>();
		Charset charset = Charset.forName("UTF-8");
		CSVFormat format = CSVFormat.DEFAULT;
		CSVParser parser = CSVParser.parse(file, charset, format);
		boolean isHeader = true;
		for (CSVRecord csvRecord : parser)
		{
			if (!isHeader)
				symbols.add(csvRecord.get(0));
			isHeader = false;
		}
		return symbols;
	}
}
