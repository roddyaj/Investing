package com.roddyaj.invest.va.model.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Position
{
	private String symbol;
	private String t0;
	private double v0;
	private double annualGrowthPct;
	private boolean sell;
	private String period;

	@JsonProperty("symbol")
	public String getSymbol()
	{
		return symbol;
	}

	@JsonProperty("symbol")
	public void setSymbol(String symbol)
	{
		this.symbol = symbol;
	}

	@JsonProperty("t0")
	public String getT0()
	{
		return t0;
	}

	@JsonProperty("t0")
	public void setT0(String t0)
	{
		this.t0 = t0;
	}

	@JsonProperty("v0")
	public double getV0()
	{
		return v0;
	}

	@JsonProperty("v0")
	public void setV0(double v0)
	{
		this.v0 = v0;
	}

	@JsonProperty("annualGrowthPct")
	public double getAnnualGrowthPct()
	{
		return annualGrowthPct;
	}

	@JsonProperty("annualGrowthPct")
	public void setAnnualGrowthPct(double annualGrowthPct)
	{
		this.annualGrowthPct = annualGrowthPct;
	}

	@JsonProperty("sell")
	public boolean getSell()
	{
		return sell;
	}

	@JsonProperty("sell")
	public void setSell(boolean sell)
	{
		this.sell = sell;
	}

	@JsonProperty("period")
	public String getPeriod()
	{
		return period;
	}

	@JsonProperty("period")
	public void setPeriod(String period)
	{
		this.period = period;
	}
}
