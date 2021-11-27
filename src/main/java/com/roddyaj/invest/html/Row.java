package com.roddyaj.invest.html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Row implements HtmlObject
{
	private final Collection<? extends HtmlObject> items;

	public Row(Collection<? extends HtmlObject> items)
	{
		this.items = items;
	}

	@Override
	public List<String> toHtml()
	{
		List<String> lines = new ArrayList<>();
		if (!isEmpty())
		{
			lines.add("<div class=\"row\">");
			for (HtmlObject item : items)
				lines.addAll(item.toHtml());
			lines.add("</div>");
		}
		return lines;
	}

	@Override
	public boolean isEmpty()
	{
		return items.stream().allMatch(HtmlObject::isEmpty);
	}
}
