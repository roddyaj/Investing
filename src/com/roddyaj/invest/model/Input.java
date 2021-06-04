package com.roddyaj.invest.model;

public class Input
{
	public final Account account;
	public final Information information;

	public Input(String accountName)
	{
		account = new Account(accountName);
		information = new Information();
	}

	public double getPrice(String symbol)
	{
		return account.getPrice(symbol);
	}
}
