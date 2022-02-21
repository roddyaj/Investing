package com.roddyaj.invest.html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
		lines.add("<base target=\"_blank\">");
		lines.add("</head>");
		lines.add("<body>");
		lines.addAll(body);
		lines.add("</body>");
		lines.add("</html>");
		return String.join("\n", lines);
	}

	public static String toLink(String url, String text)
	{
		return toLink(url, text, null);
	}

	public static String toLink(String url, String text, Map<String, ? extends Object> additionalAttributes)
	{
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("href", url);
		if (additionalAttributes != null)
			attributes.putAll(additionalAttributes);
		return tag("a", attributes, text);
	}

	public static String formatPercentChange(double d)
	{
		return color(String.format("%.2f%%", Math.abs(d)), d >= 0 ? "green" : "#C00");
	}

	public static String color(String text, String color)
	{
		return tag("span", Map.of("style", "color:" + color), text);
	}

	public static String createPopup(String content, String popupContent, boolean isText)
	{
		final String id = "pop-" + popupId++;

		StringBuilder sb = new StringBuilder();
		sb.append(startTag("div", Map.of("class", "popup", "onmouseover", "showPop('" + id + "')", "onmouseout", "hidePop('" + id + "')")));
		sb.append(content);
		Map<String, Object> attr = new HashMap<>(Map.of("class", "popup-content", "id", id));
		if (!isText)
			attr.put("style", "line-height: 0px;");
		sb.append(tag("div", attr, popupContent));
		sb.append(endTag("div"));
		return sb.toString();
	}

	public static String tag(String tag)
	{
		return tag(tag, null, null);
	}

	public static String tag(String tag, String content)
	{
		return tag(tag, null, content);
	}

	public static String tag(String tag, Map<String, ? extends Object> attributes)
	{
		return tag(tag, attributes, null);
	}

	public static String tag(String tag, Map<String, ? extends Object> attributes, String content)
	{
		StringBuilder sb = startTag_(tag, attributes);
		if (content != null)
			sb.append('>').append(content).append("</").append(tag).append('>');
		else
			sb.append(" />");
		return sb.toString();
	}

	public static String startTag(String tag, Map<String, ? extends Object> attributes)
	{
		return startTag_(tag, attributes).append('>').toString();
	}

	public static String endTag(String tag)
	{
		return new StringBuilder(tag.length() + 3).append("</").append(tag).append('>').toString();
	}

	public static String div(Map<String, ? extends Object> attributes, String content)
	{
		return tag("div", attributes, content);
	}

	private static StringBuilder startTag_(String tag, Map<String, ? extends Object> attributes)
	{
		StringBuilder sb = new StringBuilder();
		sb.append('<').append(tag);
		if (attributes != null)
		{
			for (Map.Entry<String, ? extends Object> entry : attributes.entrySet())
				sb.append(' ').append(entry.getKey()).append("=\"").append(entry.getValue()).append('"');
		}
		return sb;
	}

	private HtmlUtils()
	{
	}
}
