package com.roddyaj.invest.model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.roddyaj.invest.programs.dataroma.Dataroma;
import com.roddyaj.invest.util.AppFileUtils;
import com.roddyaj.invest.util.FileUtils;

public class Information
{
	private List<OptionableStock> optionableStocks;

	public List<OptionableStock> getOptionableStocks()
	{
		if (optionableStocks == null)
		{
			Path file = Paths.get(AppFileUtils.INPUT_DIR.toString(), "Results.csv");
			if (Files.exists(file))
				optionableStocks = FileUtils.readCsv(file).stream().filter(r -> r.getRecordNumber() > 1).map(OptionableStock::new)
						.collect(Collectors.toList());
			else
				optionableStocks = List.of();
		}
		return optionableStocks;
	}

	public List<Dataroma.Record> getDataromaStocks(String... codes)
	{
		List<Dataroma.Record> records = new ArrayList<>();
		Dataroma dataroma = new Dataroma();
		for (String code : codes)
			records.addAll(dataroma.runNoOutput(code));
		return records;
	}
}
