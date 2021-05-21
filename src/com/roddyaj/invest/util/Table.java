package com.roddyaj.invest.util;

import static com.roddyaj.invest.util.Table.Alignment.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Table
{
	public static void main(String[] args)
	{
		List<String[]> data = new ArrayList<>();
		data.add(new String[] { "hi", "there", "bro" });
		data.add(new String[] { "sup", "yo", "dog" });

		Table table = new Table(R);
		table.setHeader(new String[] { "something", "else", "there" });
		String formatted = table.format(data);
		System.out.println(formatted);
	}

	private final List<ColSetting> settings;

	private final List<String> header = new ArrayList<>();

	public Table()
	{
		this.settings = List.of();
	}

	public Table(Alignment... alignments)
	{
		this.settings = Arrays.stream(alignments).map(ColSetting::new).collect(Collectors.toList());
	}

	public Table(ColSetting... settings)
	{
		this.settings = Arrays.asList(settings);
	}

	public void setHeader(String... header)
	{
		this.header.clear();
		this.header.addAll(Arrays.asList(header));
	}

	public String format(Collection<? extends Object[]> data)
	{
		return String.join("\n", formatLines(data));
	}

	public List<String> formatLines(Collection<? extends Object[]> data)
	{
		List<String> lines = new ArrayList<>();

		List<Object[]> headerAndData = new ArrayList<>(data);
		if (!header.isEmpty())
			headerAndData.add(0, header.toArray());

		List<Integer> columnSizes = new ArrayList<>();
		for (Object[] row : headerAndData)
		{
			int c = 0;
			for (Object column : row)
			{
				if (c < columnSizes.size())
					columnSizes.set(c, Math.max(columnSizes.get(c), column.toString().length()));
				else
					columnSizes.add(column.toString().length());
				c++;
			}
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < columnSizes.size(); i++)
		{
			Integer colSize = columnSizes.get(i);
			ColSetting setting = i < settings.size() ? settings.get(i) : new ColSetting();

			if (builder.length() > 0)
				builder.append(' ');

			builder.append('%');
			if (setting.alignment == Alignment.L)
				builder.append('-');
			builder.append(colSize.intValue()).append('s');
		}
		String format = builder.toString();

		for (Object[] row : headerAndData)
			lines.add(String.format(format, row));

		return lines;
	}

	public static class ColSetting
	{
		public final Alignment alignment;

		public ColSetting()
		{
			this.alignment = Alignment.L;
		}

		public ColSetting(Alignment alignment)
		{
			this.alignment = alignment;
		}
	}

	public enum Alignment
	{
		L, R;
	}
}
