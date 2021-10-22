package com.roddyaj.invest.model;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.stream.Collectors;

import com.roddyaj.invest.html.HtmlFormatter;

public class OpenOrder
{
	private final String symbol;

	private final int quantity;

	private final double price;

	// Note, this may be null if not an option order
	private final Option option;

	public OpenOrder(String symbol, int quantity, double price, Option option)
	{
		this.symbol = symbol;
		this.quantity = quantity;
		this.price = price;
		this.option = option;
	}

	public String getSymbol()
	{
		return symbol;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public double getPrice()
	{
		return price;
	}

	public Option getOption()
	{
		return option;
	}

	@Override
	public String toString()
	{
		return "symbol=" + symbol + ", quantity=" + quantity + ", price=" + price;
	}

	public String getPopupText()
	{
		String text = String.format("%s %d @ %.2f", quantity < 0 ? "Sell" : "Buy", Math.abs(quantity), option != null ? option.getStrike() : price);
		if (option != null)
			text += String.format(" for %.2f exp. %s", price, option.getExpiryDate().format(DateTimeFormatter.ofPattern("MM-dd")));
		return text;
	}

	public static String getPopupText(Collection<? extends OpenOrder> orders)
	{
		String popupText = "";
		if (orders != null && !orders.isEmpty())
		{
			String openOrderPopupText = "Open Orders<br>" + orders.stream().map(OpenOrder::getPopupText).collect(Collectors.joining("<br>"));
			int openOrderCount = Math.abs(orders.stream().mapToInt(OpenOrder::getQuantity).sum());
			popupText = " " + HtmlFormatter.createPopup("(" + openOrderCount + ")", openOrderPopupText, true);
		}
		return popupText;
	}
}
