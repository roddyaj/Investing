package com.roddyaj.invest.programs.va2;

import java.util.ArrayList;
import java.util.List;

import com.roddyaj.invest.util.HtmlFormatter;

public class PositionManagerOutput
{
	private final String account;

	public double blah;

	public PositionManagerOutput(String account)
	{
		this.account = account;
	}

	@Override
	public String toString()
	{
		final String title = account + " Positions";
		return HtmlFormatter.toDocument(title, getContent());
	}

	public List<String> getContent()
	{
		final String title = account + " Positions";
		List<String> lines = new ArrayList<>();
		lines.add("<h2>" + title + "</h2>");
		lines.add("<div>" + blah + "</div>");
		return lines;
	}
}
