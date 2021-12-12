package com.roddyaj.invest.html;

import java.util.List;

public class RawHtml implements HtmlObject
{
	private final List<String> lines;

	public RawHtml(List<String> lines)
	{
		this.lines = lines;
	}

	@Override
	public List<String> toHtml()
	{
		return lines;
	}

	@Override
	public boolean isEmpty()
	{
		return lines.isEmpty();
	}
}
