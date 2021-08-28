package com.roddyaj.invest.model;

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
}
