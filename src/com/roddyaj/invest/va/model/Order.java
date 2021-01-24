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
		String action = shareCount >= 0 ? "\033[32mBuy \033[0m" : "\033[31mSell\033[0m";
		return String.format("%-4s %s %2d  (@ %6.2f = %4.0f)", symbol, action, Math.abs(shareCount), price, getAmount());
	}
}
