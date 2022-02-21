package com.roddyaj.invest.model;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import com.roddyaj.invest.html.HtmlUtils;

public record OpenOrder(String symbol, int quantity, double price, Option option)
{
	public Action getAction()
	{
		return quantity >= 0 ? Action.BUY : Action.SELL;
	}

	public static String getPopupText(Collection<? extends OpenOrder> orders)
	{
		String popupText = "";
		if (orders != null && !orders.isEmpty())
		{
			String openOrderPopupText = "Open Orders<br>" + orders.stream().map(OpenOrder::getPopupText).collect(Collectors.joining("<br>"));
			int openOrderCount = Math.abs(orders.stream().mapToInt(OpenOrder::quantity).sum());
			String text = HtmlUtils.tag("div", Map.of("style", "text-decoration: underline; font: 12px Arial, sans-serif;"),
				String.valueOf(openOrderCount));
			popupText = " " + HtmlUtils.createPopup(text, openOrderPopupText, true);
		}
		return popupText;
	}

	public String getPopupText()
	{
		String text = String.format("%s %d @ %.2f", quantity < 0 ? "Sell" : "Buy", Math.abs(quantity), option != null ? option.getStrike() : price);
		if (option != null)
			text += String.format(" for %.2f exp. %s", price, option.getExpiryDate().format(DateTimeFormatter.ofPattern("MM-dd")));
		return text;
	}
}
