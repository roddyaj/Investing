package com.roddyaj.invest.model;

import java.util.ArrayList;
import java.util.List;

public class Lots
{
	private final List<Lot> lots = new ArrayList<>();

	public void add(int quantity, double price)
	{
		lots.add(new Lot(quantity, price));
	}

	public void removeFifo(int quantity)
	{
		int sellCount = quantity;
		for (int i = 0; i < lots.size() && sellCount > 0; i++)
		{
			Lot lot = lots.get(i);
			if (lot.quantity <= sellCount)
			{
				lots.remove(i--);
				sellCount -= lot.quantity;
			}
			else
			{
				lot.quantity -= sellCount;
				sellCount = 0;
			}
		}
	}

	public double getCostBasis()
	{
		double costBasis = 0;
		if (!lots.isEmpty())
		{
			costBasis = lots.stream().mapToDouble(lot -> lot.quantity * lot.price).sum();
			costBasis = Math.round(costBasis * 100) / 100.;
		}
		return costBasis;
	}

	private static class Lot
	{
		public int quantity;
		public final double price;

		public Lot(int quantity, double price)
		{
			this.quantity = quantity;
			this.price = price;
		}

		@Override
		public String toString()
		{
			return quantity + " @ " + price;
		}
	}
}
