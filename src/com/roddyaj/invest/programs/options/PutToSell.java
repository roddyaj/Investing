package com.roddyaj.invest.programs.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PutToSell implements Comparable<PutToSell>
{
	public final String symbol;
	public final double availableAmount;
	public double averageReturn;

	public PutToSell(String symbol, double availableAmount)
	{
		this.symbol = symbol;
		this.availableAmount = availableAmount;
	}

	@Override
	public String toString()
	{
		return String.format("%-4s $%4.0f available (%3.0f%% return)", symbol, availableAmount, averageReturn);
	}

	@Override
	public int compareTo(PutToSell o)
	{
		return Double.compare(o.averageReturn, averageReturn);
	}

	public String toHtmlString()
	{
		final String url = "https://client.schwab.com/Areas/Trade/Options/Chains/Index.aspx#symbol/%s";
		return String.format("<tr><td><a href=\"" + url + "\">%s</a></td><td align=\"right\">$%.0f</td><td align=\"right\">%.0f%%</td></tr>", symbol,
				symbol, availableAmount, averageReturn);
	}

	public static List<String> toHtml(Collection<? extends PutToSell> puts)
	{
		List<String> lines = new ArrayList<>();
		if (!puts.isEmpty())
		{
			lines.add("<h4>Candidate Puts To Sell</h4>");
			lines.add("<table cellspacing=\"10\">");
			lines.add("<tr><th>Ticker</th><th>Available</th><th>Avg. Return</th></tr>");
			puts.forEach(p -> lines.add(p.toHtmlString()));
			lines.add("</table>");
		}
		return lines;
	}
}
