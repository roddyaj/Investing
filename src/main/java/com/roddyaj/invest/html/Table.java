package com.roddyaj.invest.html;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Table implements HtmlObject
{
	private final List<Column> columns;

	private final List<List<Object>> rows;

	private boolean showHeader = true;

	public Table(List<Column> columns, List<List<Object>> rows)
	{
		this.columns = columns;
		this.rows = rows;
	}

	public final void setShowHeader(boolean showHeader)
	{
		this.showHeader = showHeader;
	}

	public int getRowCount()
	{
		return rows.size();
	}

	public String toHtmlSingleLine()
	{
		if (isEmpty())
			return "";

		StringBuilder content = new StringBuilder();
		if (showHeader)
			content.append(formatHeader());
		for (List<Object> row : rows)
			content.append(formatRow(row));
		return HtmlUtils.tag("table", content.toString());
	}

	@Override
	public List<String> toHtml()
	{
		List<String> lines = new ArrayList<>(3 + rows.size());
		if (!isEmpty())
		{
			lines.add("<table>");
			if (showHeader)
				lines.add(formatHeader());
			for (List<Object> row : rows)
				lines.add(formatRow(row));
			lines.add("</table>");
		}
		return lines;
	}

	@Override
	public boolean isEmpty()
	{
		return rows.isEmpty();
	}

	private String formatHeader()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("<tr>");
		int i = 0;
		for (Column column : columns)
		{
			List<String> classes = new ArrayList<>();
			if (column.align != Align.R)
				classes.add(column.align.toString());
			if (i++ == 0)
				classes.add("f");
			Map<String, Object> attributes = classes.isEmpty() ? null : Map.of("class", String.join(" ", classes));

			builder.append(HtmlUtils.tag("th", attributes, column.name));
		}
		builder.append("</tr>");
		return builder.toString();
	}

	private String formatRow(List<Object> row)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("<tr>");
		int i = 0;
		for (Object cell : row)
		{
			Column column = columns.get(i);

			List<String> classes = new ArrayList<>();
			if (column.align != Align.R)
				classes.add(column.align.toString());
			if (i++ == 0)
				classes.add("f");
			Map<String, Object> attributes = classes.isEmpty() ? null : Map.of("class", String.join(" ", classes));

			builder.append(HtmlUtils.tag("td", attributes, cell != null ? String.format(column.format, cell) : ""));
		}
		builder.append("</tr>");
		return builder.toString();
	}

	public static class Column
	{
		public final String name;
		public final String format;
		public final Align align;

		public Column(String name, String format, Align align)
		{
			this.name = name;
			this.format = format;
			this.align = align;
		}
	}

	public static enum Align
	{
		L("l"), C("c"), R("r");

		private final String text;

		private Align(String text)
		{
			this.text = text;
		}

		@Override
		public String toString()
		{
			return text;
		}
	}
}
