package com.roddyaj.invest.programs.dataroma;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.roddyaj.invest.model.Program;
import com.roddyaj.invest.network.HttpClient;

public class Dataroma implements Program
{
	@Override
	public void run(String[] args)
	{
		String url = args[0];
		try
		{
			String content = HttpClient.get(url);
			List<Record> records = parseRecords(content);
			records = filterRecords(records);
			String csv = toYahooCsv(records);
			System.out.println(csv);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private List<Record> parseRecords(String content)
	{
		final Pattern stockPattern = Pattern.compile("<td class=\"stock\"><a href=.+?>(.+?)<span>.+?</span></a></td>");
		final Pattern buyPattern = Pattern.compile("<td class=\"buy\">(.+?)</td>");

		List<Record> records = new ArrayList<>();
		content.lines().forEach(line -> {
			Matcher matcher = stockPattern.matcher(line);
			if (matcher.find())
			{
				String ticker = matcher.group(1);
				records.add(new Record(ticker));
			}
			else
			{
				matcher = buyPattern.matcher(line);
				if (matcher.find())
				{
					String activity = matcher.group(1);
					records.get(records.size() - 1).activity = activity;
				}
			}
		});
		return records;
	}

	private List<Record> filterRecords(Collection<? extends Record> records)
	{
		final Pattern addPattern = Pattern.compile("Add (.+?)%");

		return records.stream().filter(record -> {
			boolean pass = false;
			String activity = record.activity;
			if (activity != null)
			{
				pass = activity.startsWith("Buy");
				if (!pass)
				{
					Matcher matcher = addPattern.matcher(activity);
					if (matcher.find())
					{
						double percent = Double.parseDouble(matcher.group(1));
						pass = percent > 2;
					}
				}
			}
			return pass;
		}).collect(Collectors.toList());
	}

	private String toYahooCsv(List<Record> records)
	{
		Collections.reverse(records);
		return records.stream().map(r -> r.ticker).collect(Collectors.joining(","));
	}

	private static class Record
	{
		public String ticker;

		public String activity;

		public Record(String ticker)
		{
			this.ticker = ticker;
		}

		@Override
		public String toString()
		{
			return ticker + " " + activity;
		}
	}
}
