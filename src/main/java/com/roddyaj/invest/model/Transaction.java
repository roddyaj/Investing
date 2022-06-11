package com.roddyaj.invest.model;

import java.time.LocalDate;
import java.util.Objects;

import com.roddyaj.invest.util.StringUtils;

public record Transaction(int index, LocalDate date, Action action, String symbol, int quantity, double price, double amount, Option option)
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

	public int getCollapsibleIdentifier()
	{
		return Objects.hash(date, action, symbol, price, option != null ? System.identityHashCode(option) : 0);
	}

	public static Transaction collapse(Transaction a, Transaction b)
	{
		return a == null ? b : new Transaction(a.index, a.date, a.action, a.symbol, a.quantity + b.quantity, a.price, a.amount + b.amount, a.option);
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
