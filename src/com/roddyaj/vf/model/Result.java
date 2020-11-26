package com.roddyaj.vf.model;

public class Result
{
	public final String message;

	public final boolean pass;

	public final Double price;

	public Result(String message, boolean pass)
	{
		this.message = message;
		this.pass = pass;
		this.price = null;
	}

	public Result(String message, boolean pass, Double price)
	{
		this.message = message;
		this.pass = pass;
		this.price = price;
	}
}
