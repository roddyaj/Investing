package com.roddyaj.invest.model.settings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Allocation
{
	private String cat;
	private double percent;

	public Allocation()
	{
	}

	public Allocation(String cat, double percent)
	{
		this.cat = cat;
		this.percent = percent;
	}

	@JsonProperty("cat")
	public String getCat()
	{
		return cat;
	}

	@JsonProperty("cat")
	public void setCat(String cat)
	{
		this.cat = cat;
	}

	@JsonIgnore
	public String getCatLastToken()
	{
		String[] tokens = cat.split("\\.");
		return tokens[tokens.length - 1];
	}

	@JsonProperty("%")
	public double getPercent()
	{
		return percent;
	}

	@JsonProperty("%")
	public void setPercent(double percent)
	{
		this.percent = percent;
	}

	@Override
	public String toString()
	{
		return cat + ": " + percent;
	}
}
