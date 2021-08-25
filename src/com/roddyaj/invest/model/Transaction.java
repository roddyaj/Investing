package com.roddyaj.invest.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.roddyaj.invest.util.StringUtils;

public class Transaction
{
	private final LocalDate date;
	private final Action action;
	private final String symbol;
	private final int quantity;
	private final double price;
	private final double amount;

	private final Option option;
	private final int days;
	private final double annualReturn;

	public Transaction(LocalDate date, Action action, String symbol, int quantity, double price, double amount, Option option)
	{
		this.date = date;
		this.action = action;
		this.symbol = symbol;
		this.quantity = quantity;
		this.price = price;
		this.amount = amount;

		this.option = option;
		days = option != null ? (int)ChronoUnit.DAYS.between(date, option.getExpiryDate()) : 0;
		annualReturn = option != null ? ((amount / quantity) / option.getStrike()) * (365.0 / days) : 0;
	}

	public LocalDate getDate()
	{
		return date;
	}

	public Action getAction()
	{
		return action;
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

	public double getAmount()
	{
		return amount;
	}

	public Option getOption()
	{
		return option;
	}

	public int getDays()
	{
		return days;
	}

	public double getAnnualReturn()
	{
		return annualReturn;
	}

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
		return isOption() ? toStringOption() : toStringStock();
	}

	private static final String STOCK_FORMAT = "%s %-14s %-5s %3d %6.2f %8.2f";
	private static final String OPTION_FORMAT = STOCK_FORMAT + "  %s %5.2f %s %2dd (%5.1f%%)";

	private String toStringStock()
	{
		return String.format(STOCK_FORMAT, date, StringUtils.limit(action.toString(), 14), symbol, quantity, price, amount);
	}

	private String toStringOption()
	{
		return String.format(OPTION_FORMAT, date, StringUtils.limit(action.toString(), 14), symbol, quantity, price, amount, option.getExpiryDate(),
				option.getStrike(), option.getType(), days, annualReturn);
	}
}
