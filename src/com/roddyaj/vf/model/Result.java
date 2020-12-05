package com.roddyaj.vf.model;

public class Result
{
	public final String property;

	public final Object value;

	public final Boolean pass;

	public Result(String property, Object value)
	{
		this.property = property;
		this.value = value;
		this.pass = null;
	}

	public Result(String property, Object value, boolean pass)
	{
		this.property = property;
		this.value = value;
		this.pass = pass;
	}
}
