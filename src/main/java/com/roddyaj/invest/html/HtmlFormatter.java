package com.roddyaj.invest.html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.roddyaj.invest.html.Table.Align;

public abstract class HtmlFormatter
{
	private static int popupId;

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
				a:visited { color: blue; }
				.row { display: flex; flex-direction: row; }
				.column { display: flex; flex-direction: column; margin-right: 8px; }
				.block { border: 1px solid; border-radius: 4px; padding: 4px; margin-bottom: 8px; background-color: #F7F7F7; }
				.heading { display: flex; align-items: center; padding-bottom: 4px; margin-bottom: 4px; border-bottom: 1px solid; }
				.title { font-size: large; font-weight: bold; }
				.popup {
					position: relative;
					display: inline-block;
					cursor: pointer;
				}
				.popup-text {
					font: 12px Arial, sans-serif;
				}
				.popup .popup-content {
					visibility: hidden;
					white-space: nowrap;
					position: absolute;
					background-color: white;
					padding: 4px 4px;
					border: 1px solid;
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

	public static String toLinkSymbol(String url, String symbol)
	{
		return symbol.toUpperCase().equals(symbol) ? toLink(String.format(url, symbol), symbol) : symbol;
	}

	public static String toLink(String url, String text)
	{
		return String.format("<a href=\"%s\" target=\"_blank\">%s</a>", url, text);
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

		final String id = "popup-" + popupId++;

		sb.append("<div");
		appendKeyValue(sb, "class", isText ? "popup popup-text" : "popup");
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

	private static void appendKeyValue(StringBuilder sb, String key, String value)
	{
		sb.append(' ').append(key).append("=\"").append(value).append('"');
	}
}
