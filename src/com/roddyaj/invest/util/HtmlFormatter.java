package com.roddyaj.invest.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class HtmlFormatter<T> implements Formatter<T>
{
	@Override
	public String getHeader()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("<tr>");
		for (Column column : getColumns())
			builder.append("<th style=\"text-align: ").append(column.align).append("\">").append(column.name).append("</th>");
		builder.append("</tr>");
		return builder.toString();
	}

	@Override
	public String format(T object)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("<tr>");
		List<Column> columns = getColumns();
		int i = 0;
		for (Object element : getObjectElements(object))
		{
			Column column = columns.get(i++);
			builder.append("<td style=\"text-align: ").append(column.align).append("\">").append(String.format(column.format, element))
					.append("</td>");
		}
		builder.append("</tr>");
		return builder.toString();
	}

	@Override
	public List<String> format(Collection<? extends T> objects)
	{
		List<String> lines = new ArrayList<>(3 + objects.size());
		lines.add("<table>");
		lines.add(getHeader());
		for (T object : objects)
			lines.add(format(object));
		lines.add("</table>");
		return lines;
	}

	public List<String> toBlock(Collection<? extends T> objects, String title)
	{
		List<String> lines = new ArrayList<>();
		if (!objects.isEmpty())
		{
			lines.add("<div class=\"block\">");
			lines.add("<div class=\"heading\"><b>" + title + "</b></div>");
			lines.addAll(format(objects));
			lines.add("</div>");
		}
		return lines;
	}

	public List<String> toBlockHtmlTitle(Collection<? extends T> objects, String titleHtml)
	{
		List<String> lines = new ArrayList<>();
		if (!objects.isEmpty())
		{
			lines.add("<div class=\"block\">");
			lines.add(titleHtml);
			lines.addAll(format(objects));
			lines.add("</div>");
		}
		return lines;
	}

	protected abstract List<Column> getColumns();

	protected abstract List<Object> getObjectElements(T object);

	public static String toLink(String url, String text)
	{
		return String.format("<a href=\"" + url + "\">%s</a>", text, text);
	}

	public static String toDocument(String title, Collection<? extends String> input)
	{
		List<String> lines = new ArrayList<>();
		lines.add("<!DOCTYPE html>\n<html lang=\"en\">\n<head>");
		lines.add("<title>" + title + "</title>");
		lines.add("<style>");
		lines.add("th, td { padding: 2px 4px; }");
		lines.add(".heading { margin-bottom: 4px; font-size: large; }");
		lines.add(".block { border-style: solid; border-width: 1px; padding: 4px; margin: 8px 0px; }");
		lines.add("</style>");
		lines.add("</head>\n<body>");
		lines.addAll(input);
		lines.add("</body>\n</html>");
		return String.join("\n", lines);
	}

	public enum Align
	{
		L("left"), C("center"), R("right");

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
	};

	public class Column
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
}
