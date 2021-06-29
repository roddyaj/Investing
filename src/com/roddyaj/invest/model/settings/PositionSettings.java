package com.roddyaj.invest.model.settings;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PositionSettings
{
	private String symbol;
	private String t0;
	private double v0;
	private Boolean sell;
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

	@JsonProperty("sell")
	public Boolean getSell()
	{
		return sell;
	}

	@JsonProperty("sell")
	public void setSell(Boolean sell)
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

	@Override
	public String toString()
	{
		String symbolText = "\"" + symbol + "\",";
		String sellText = sell != null && sell.booleanValue() ? ", \"sell\": true" : "";
		return String.format("        { \"symbol\": %-8s \"t0\": \"%s\", \"v0\": %5.0f%s },", symbolText, t0, v0, sellText);
	}
}
