package com.roddyaj.invest.html;

import java.util.ArrayList;
import java.util.List;

public class Block implements HtmlObject
{
	private final String title;
	private final String info;
	private final HtmlObject content;

	public Block(String title, String info, HtmlObject content)
	{
		this.title = title;
		this.info = info;
		this.content = content;
	}

	@Override
	public List<String> toHtml()
	{
		List<String> lines = new ArrayList<>();
		if (!isEmpty())
		{
			lines.add("<div class=\"block\">");
			String heading = formatHeading();
			if (heading != null)
				lines.add(heading);
			lines.addAll(content.toHtml());
			lines.add("</div>");
		}
		return lines;
	}

	@Override
	public boolean isEmpty()
	{
		return content.isEmpty();
	}

	private String formatHeading()
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
}
