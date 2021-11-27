package com.roddyaj.invest.html;

import java.util.ArrayList;
import java.util.List;

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

	public void setShowHeader(boolean showHeader)
	{
		this.showHeader = showHeader;
	}

	public int getRowCount()
	{
		return rows.size();
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
		for (Column column : columns)
		{
			builder.append("<th");
			if (column.align != Align.C)
				builder.append(" class=\"").append(column.align).append("\"");
			builder.append(">");
			builder.append(column.name).append("</th>");
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
			Column column = columns.get(i++);
			builder.append("<td");
			if (column.align != Align.L)
				builder.append(" class=\"").append(column.align).append("\"");
			builder.append(">");
			if (cell != null)
				builder.append(String.format(column.format, cell));
			builder.append("</td>");
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
