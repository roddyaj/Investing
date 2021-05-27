package com.roddyaj.invest.programs.dataroma;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.roddyaj.invest.framework.Program;
import com.roddyaj.invest.network.HttpClient;

public class Dataroma implements Program
{
	// For testing in IDE
	public static void main(String[] args)
	{
		new Dataroma().run(new String[] { "https://www.dataroma.com/m/holdings.php?m=SAM" });
	}

	@Override
	public void run(String[] args)
	{
		String url = args[0];
		try
		{
			String content = HttpClient.get(url);
			List<Record> records = parseRecords(content);
			records = filterRecords(records);
			normalizePercents(records);

			printSettings(records, url);
			System.out.println(toYahooCsv(records));
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
		final Pattern percentPattern = Pattern.compile("<td>(\\d+\\.\\d{2})</td>");

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
				else
				{
					matcher = percentPattern.matcher(line);
					if (matcher.find())
					{
						double percent = Double.parseDouble(matcher.group(1));
						records.get(records.size() - 1).percent = percent;
					}
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
			pass &= record.percent >= 1.5;
			return pass;
		}).collect(Collectors.toList());
	}

	private void normalizePercents(Collection<? extends Record> records)
	{
		double totalPercent = records.stream().mapToDouble(r -> r.percent).sum() / 100;
		records.forEach(r -> r.percent /= totalPercent);
	}

	private String toYahooCsv(List<Record> records)
	{
		Collections.reverse(records);
		return records.stream().map(r -> r.ticker).collect(Collectors.joining(","));
	}

	private void printSettings(Collection<? extends Record> records, String url)
	{
		int i = url.lastIndexOf('=');
		final String code = url.substring(i + 1).toLowerCase();
		records.forEach(r -> {
			String categoryString = String.format("%-28s", String.format("\"me.risk.tracked.%s.%s\",", code, r.ticker));
			System.out.println(String.format("        { \"cat\": %s \"%%\": %6.3f },", categoryString, r.percent));
		});
	}

	private static class Record
	{
		public String ticker;

		public String activity;

		public double percent;

		public Record(String ticker)
		{
			this.ticker = ticker;
		}

		@Override
		public String toString()
		{
			return ticker + " " + activity + " " + percent;
		}
	}
}
