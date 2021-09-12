package com.roddyaj.invest.model;

import java.util.Collection;
import java.util.stream.Collectors;

import com.roddyaj.invest.util.HtmlFormatter;

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

//	public double getAmount()
//	{
//		return quantity * price;
//	}

	@Override
	public String toString()
	{
		return "symbol=" + symbol + ", quantity=" + quantity + ", price=" + price;
	}

	public static String getPopupText(Collection<? extends OpenOrder> orders)
	{
		String popupText = "";
		if (orders != null && !orders.isEmpty())
		{
			String openOrderPopupText = "Open Orders<br>" + orders.stream().map(o -> {
				String action = o.getQuantity() < 0 ? "Sell" : "Buy";
				String price = String.format("%.2f", o.getOption() != null ? o.getOption().getStrike() : o.getPrice());
				return action + " " + Math.abs(o.getQuantity()) + " @ " + price;
			}).collect(Collectors.joining("<br>"));
			int openOrderCount = Math.abs(orders.stream().mapToInt(OpenOrder::getQuantity).sum());
			popupText = " " + HtmlFormatter.createPopup("(" + openOrderCount + ")", openOrderPopupText, true);
		}
		return popupText;
	}
}
