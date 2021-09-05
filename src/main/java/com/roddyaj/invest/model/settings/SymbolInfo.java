package com.roddyaj.invest.model.settings;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SymbolInfo
{
	private String symbol;
	private double annualGrowthPct;

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
}
