package com.roddyaj.invest.html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.roddyaj.invest.util.FileUtils;

public final class HtmlUtils
{
	private static int popupId;

	public static String toDocument(String title, Collection<? extends String> body)
	{
		List<String> lines = new ArrayList<>();
		lines.add("<!DOCTYPE html>");
		lines.add("<html lang=\"en\">");
		lines.add("<head>");
		lines.add("<meta charset=\"utf-8\" />");
		lines.add("<title>" + title + "</title>");
		lines.add("<style>");
		lines.addAll(FileUtils.readResourceLines("output.css"));
		lines.add("</style>");
		lines.add("<script>");
		lines.addAll(FileUtils.readResourceLines("output.js"));
		lines.add("</script>");
		lines.add("</head>");
		lines.add("<body>");
		lines.addAll(body);
		lines.add("</body>");
		lines.add("</html>");
		return String.join("\n", lines);
	}

	public static String toLinkSymbol(String url, String symbol)
	{
		return symbol.toUpperCase().equals(symbol) ? toLink(String.format(url, symbol), symbol) : symbol;
	}

	public static String toLink(String url, String text)
	{
		return toLink(url, text, "");
	}

	public static String toLink(String url, String text, String additionalProperties)
	{
		return String.format("<a href=\"%s\" target=\"_blank\"%s>%s</a>", url, additionalProperties, text);
	}

	public static String formatPercentChange(double d)
	{
		return color(String.format("%.2f%%", Math.abs(d)), d >= 0 ? "green" : "red");
	}

	public static String color(double d, String format)
	{
		return color(String.format(format, d), d >= 0 ? "green" : "red");
	}

	public static String color(String text, String color)
	{
		return "<span style=\"color:" + color + "\">" + text + "</span>";
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

	public static String createPopup(String content, String popupContent, boolean isText)
	{
		StringBuilder sb = new StringBuilder();

		final String id = "pop-" + popupId++;

		sb.append("<div");
		appendKeyValue(sb, "class", "popup");
		sb.append(" onmouseover=\"showPopup('").append(id).append("')\"");
		sb.append(" onmouseout=\"hidePopup('").append(id).append("')\"");
		sb.append('>');

		sb.append(content);

		sb.append("<div");
		appendKeyValue(sb, "class", "popup-content");
		appendKeyValue(sb, "id", id);
		if (!isText)
			appendKeyValue(sb, "style", "line-height: 0px;");
		sb.append('>');
		sb.append(popupContent).append("</div>");

		sb.append("</div>");

		return sb.toString();
	}

	public static String startTag(String tag, Map<String, ? extends Object> attributes)
	{
		StringBuilder sb = new StringBuilder();
		sb.append('<').append(tag);
		for (Map.Entry<String, ? extends Object> entry : attributes.entrySet())
			appendKeyValue(sb, entry.getKey(), entry.getValue());
		sb.append('>');
		return sb.toString();
	}

	public static String tag(String tag, Map<String, ? extends Object> attributes)
	{
		StringBuilder sb = new StringBuilder();
		sb.append('<').append(tag);
		for (Map.Entry<String, ? extends Object> entry : attributes.entrySet())
			appendKeyValue(sb, entry.getKey(), entry.getValue());
		sb.append(" />");
		return sb.toString();
	}

	private static void appendKeyValue(StringBuilder sb, String key, Object value)
	{
		sb.append(' ').append(key).append("=\"").append(value).append('"');
	}

	private HtmlUtils()
	{
	}
}
