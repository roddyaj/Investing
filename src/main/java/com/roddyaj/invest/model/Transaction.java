package com.roddyaj.invest.model;

import java.time.LocalDate;

import com.roddyaj.invest.util.StringUtils;

public record Transaction(LocalDate date, Action action, String symbol, int quantity, double price, double amount, Option option)
{
	public boolean isOption()
	{
		return option != null;
	}

	public boolean isCallOption()
	{
		return isOption() && option.getType() == 'C';
	}

	public boolean isPutOption()
	{
		return isOption() && option.getType() == 'P';
	}

	@Override
	public String toString()
	{
		String actionText = action != null ? StringUtils.limit(action.toString(), 14) : "";
		String text = String.format("%s %-14s %-5s %3d %6.2f %8.2f", date, actionText, symbol, quantity, price, amount);
		if (option != null)
			text += (" " + option.toString());
		return text;
	}
}
