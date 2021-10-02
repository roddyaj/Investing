package com.roddyaj.invest.html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Column implements HtmlObject
{
	private final Collection<? extends HtmlObject> items;

	public Column(Collection<? extends HtmlObject> items)
	{
		this.items = items;
	}

	@Override
	public List<String> toHtml()
	{
		List<String> lines = new ArrayList<>();
		lines.add("<div class=\"column\">");
		for (HtmlObject item : items)
			lines.addAll(item.toHtml());
		lines.add("</div>");
		return lines;
	}
}
