package com.roddyaj.invest.model;

public enum Action
{
	BUY("Buy"),
	SELL("Sell"),
	SELL_TO_OPEN("Sell to Open"),
	BUY_TO_CLOSE("Buy To Close"),
	BUY_TO_OPEN("Buy To Open"),
	SELL_TO_CLOSE("Sell To Close"),
	TRANSFER("Transfer"),
	DIVIDEND("Dividend");

	private final String text;

	private Action(String text)
	{
		this.text = text;
	}

	@Override
	public String toString()
	{
		return text;
	}
}
