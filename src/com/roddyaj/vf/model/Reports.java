package com.roddyaj.vf.model;

import java.util.ArrayList;
import java.util.Collection;
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
		format(fails, "Fail", lines);
		lines.add("");
		format(passes, "Pass", lines);
		return String.join("\n", lines);
	}

	private void format(Collection<? extends Report> reports, String title, Collection<? super String> lines)
	{
		lines.add("=================== " + title + " ===================");
		for (Report report : reports)
		{
			lines.add("");
			lines.add(report.toString());
		}
	}
}
