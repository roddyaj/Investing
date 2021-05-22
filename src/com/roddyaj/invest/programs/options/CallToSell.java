package com.roddyaj.invest.programs.options;

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
}
