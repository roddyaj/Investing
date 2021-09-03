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
			builder.append("<th class=\"").append(column.align).append("\">").append(column.name).append("</th>");
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
			builder.append("<td class=\"").append(column.align).append("\">");
			if (element != null)
				builder.append(String.format(column.format, element));
			builder.append("</td>");
		}
		builder.append("</tr>");
		return builder.toString();
	}

	@Override
	public List<String> format(Collection<? extends T> objects)
	{
		List<String> lines = new ArrayList<>(3 + objects.size());
		lines.add("<table>");
		String header = getHeader();
		if (header != null)
			lines.add(header);
		for (T object : objects)
			lines.add(format(object));
		lines.add("</table>");
		return lines;
	}

	public List<String> toBlock(Collection<? extends T> objects, String title, String info)
	{
		List<String> lines = new ArrayList<>();
		if (!objects.isEmpty())
		{
			lines.add("<div class=\"block\">");
			String heading = toHeading(title, info);
			if (heading != null)
				lines.add(heading);
			lines.addAll(format(objects));
			lines.add("</div>");
		}
		return lines;
	}

	protected abstract List<Column> getColumns();

	protected abstract List<Object> getObjectElements(T object);

	public static String toDocument(String title, Collection<? extends String> input)
	{
		List<String> lines = new ArrayList<>();
		lines.add("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n<meta charset=\"utf-8\" />");
		lines.add("<title>" + title + "</title>");
		lines.add("<style>");
		lines.add("""
				body { font: 14px Arial, sans-serif; }
				th, td { padding: 2px 4px; }
				a:link { text-decoration: none; }
				a:hover { text-decoration: underline; }
				.row { display: flex; flex-direction: row; }
				.column { display: flex; flex-direction: column; margin-right: 8px; }
				.block { border-style: solid; border-width: 1px; border-radius: 4px; padding: 4px; margin-bottom: 8px; background-color: #F7F7F7; }
				.heading { display: flex; align-items: center; padding-bottom: 4px; margin-bottom: 4px; border-bottom: 1px solid; }
				.title { font-size: large; font-weight: bold; }
				.popup { position: relative; display: inline-block; cursor: pointer; }
				.popup .popuptext {
					visibility: hidden;
					position: absolute;
					background-color: #555;
					color: #fff;
					padding: 4px 4px;
					border-radius: 4px;
					left: 12px;
					bottom: 18px;
				}
				.popup .show { visibility: visible; }""");
		lines.add("." + Align.L + " { text-align: left; }");
		lines.add("." + Align.R + " { text-align: right; }");
		lines.add("." + Align.C + " { text-align: center; }");
		lines.add("</style>");
		lines.add("""
				<script>
				function showPopup(id) {
					const popup = document.getElementById(id);
					popup.classList.add("show");
				}
				function hidePopup(id) {
					const popup = document.getElementById(id);
					popup.classList.remove("show");
				}
				</script>""");
		lines.add("</head>\n<body>");
		lines.addAll(input);
		lines.add("</body>\n</html>");
		return String.join("\n", lines);
	}

	public static List<String> toColumn(Collection<? extends String> input)
	{
		List<String> lines = new ArrayList<>(input.size() + 2);
		lines.add("<div class=\"column\">");
		lines.addAll(input);
		lines.add("</div>");
		return lines;
	}

	public static List<String> toRow(Collection<? extends String> input)
	{
		List<String> lines = new ArrayList<>(input.size() + 2);
		lines.add("<div class=\"row\">");
		lines.addAll(input);
		lines.add("</div>");
		return lines;
	}

	public static String toHeading(String title, String info)
	{
		if (title == null)
			return null;

		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"heading\">");
		sb.append("<div class=\"title\">").append(title).append("</div>");
		if (info != null)
			sb.append("<div style=\"margin-left: 8px;\">").append(info).append("</div>");
		sb.append("</div>");
		return sb.toString();
	}

	public static String toLinkSymbol(String url, String symbol)
	{
		return toLink(String.format(url, symbol), symbol);
	}

	public static String toLink(String url, String text)
	{
		return String.format("<a href=\"%s\" target=\"_blank\">%s</a>", url, text);
	}

	public static String color(String text, String color)
	{
		return "<span style=\"color:" + color + "\">" + text + "</span>";
	}

	public static String color(double d, String format)
	{
		return color(String.format(format, d), d >= 0 ? "green" : "red");
	}

	public static List<String> toSimpleColumnTable(Collection<? extends String> columns)
	{
		List<String> lines = new ArrayList<>();
		lines.add("<div class=\"block\">");
		lines.add("<table>");
		StringBuilder builder = new StringBuilder();
		builder.append("<tr>");
		for (String column : columns)
			builder.append("<td>").append(column).append("</td>");
		builder.append("</tr>");
		lines.add(builder.toString());
		lines.add("</table>");
		lines.add("</div>");
		return lines;
	}

	public String createPopup(String content, String popupContent, String id)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"popup\" onmouseover=\"showPopup('").append(id).append("')\" onmouseout=\"hidePopup('").append(id).append("')\">");
		sb.append(content);
		sb.append("<div class=\"popuptext\" id=\"").append(id).append("\">").append(popupContent).append("</div>");
		sb.append("</div>");
		return sb.toString();
	}

	public enum Align
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
