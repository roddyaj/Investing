package com.roddyaj.invest.programs.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.roddyaj.invest.model.Position;

public class CallToSell
{
	public final Position position;
	public final double lastBuy;
	public final int quantity;

	public CallToSell(Position position, double lastBuy, int quantity)
	{
		this.position = position;
		this.lastBuy = lastBuy;
		this.quantity = quantity;
	}

	@Override
	public String toString()
	{
		return String.format("%-4s %d %s (bought at $%5.2f)", position.symbol, quantity, position.dayChangePct >= 0 ? "Y" : " ", lastBuy);
	}

	public String toHtmlString()
	{
		final String url = "https://client.schwab.com/Areas/Trade/Options/Chains/Index.aspx#symbol/%s";
		return String.format("<tr><td><a href=\"" + url + "\">%s</a></td><td>%d</td><td align=\"center\">%s</td><td align=\"right\">$%.2f</td></tr>",
				position.symbol, position.symbol, quantity, position.dayChangePct >= 0 ? "Y" : "", lastBuy);
	}

	public static List<String> toHtml(Collection<? extends CallToSell> calls)
	{
		List<String> lines = new ArrayList<>();
		if (!calls.isEmpty())
		{
			lines.add("<h4>Calls To Sell</h4>");
			lines.add("<table cellspacing=\"10\">");
			lines.add("<tr><th>Ticker</th><th>#</th><th>Favorable</th><th>Last Buy</th></tr>");
			calls.forEach(c -> lines.add(c.toHtmlString()));
			lines.add("</table>");
		}
		return lines;
	}
}
