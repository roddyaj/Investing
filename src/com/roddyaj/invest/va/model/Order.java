package com.roddyaj.invest.va.model;

public class Order
{
	public final String symbol;

	public final int shareCount;

	public final double price;

	public Order(String symbol, int shareCount, double price)
	{
		this.symbol = symbol;
		this.shareCount = shareCount;
		this.price = price;
	}

	public double getAmount()
	{
		return shareCount * price;
	}

	@Override
	public String toString()
	{
		String action = shareCount >= 0 ? "Buy" : "Sell";
		return String.format("%-4s %-4s %2d  (@ %.2f = %.0f)", symbol, action, Math.abs(shareCount), price, getAmount());
	}
}
