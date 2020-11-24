package com.roddyaj.vf.model;

import java.util.ArrayList;
import java.util.List;

public class Reports
{
	private final List<Report> passes = new ArrayList<>();

	private final List<Report> fails = new ArrayList<>();

	public void addReport(Report report)
	{
		if (report.pass)
			passes.add(report);
		else
			fails.add(report);
	}

	@Override
	public String toString()
	{
		List<String> lines = new ArrayList<>();
		lines.add("Fail:");
		for (Report report : fails)
			lines.add(report.toString());
		lines.add("");
		lines.add("Pass:");
		for (Report report : passes)
			lines.add(report.toString());
		return String.join("\n", lines);
	}
}
