package com.roddyaj.invest.model.settings;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Settings
{
	private String defaultDataDir;

	private double defaultAnnualGrowthPct;

	private String[] optionsExclude;

	private String[] optionsInclude;

	private SymbolInfo[] symbolInfos;

	private AccountSettings[] accounts;

	@JsonProperty("defaultDataDir")
	public String getDefaultDataDir()
	{
		return defaultDataDir;
	}

	@JsonProperty("defaultDataDir")
	public void setDefaultDataDir(String defaultDataDir)
	{
		this.defaultDataDir = defaultDataDir;
	}

	@JsonProperty("defaultAnnualGrowthPct")
	public double getDefaultAnnualGrowthPct()
	{
		return defaultAnnualGrowthPct;
	}

	@JsonProperty("defaultAnnualGrowthPct")
	public void setDefaultAnnualGrowthPct(double defaultAnnualGrowthPct)
	{
		this.defaultAnnualGrowthPct = defaultAnnualGrowthPct;
	}

	@JsonProperty("optionsExclude")
	public String[] getOptionsExclude()
	{
		return optionsExclude;
	}

	@JsonProperty("optionsExclude")
	public void setOptionsExclude(String[] optionsExclude)
	{
		this.optionsExclude = optionsExclude;
	}

	@JsonProperty("optionsInclude")
	public String[] getOptionsInclude()
	{
		return optionsInclude;
	}

	@JsonProperty("optionsInclude")
	public void setOptionsInclude(String[] optionsInclude)
	{
		this.optionsInclude = optionsInclude;
	}

	@JsonProperty("symbolInfos")
	public SymbolInfo[] getSymbolInfos()
	{
		return symbolInfos;
	}

	@JsonProperty("symbolInfos")
	public void setSymbolInfos(SymbolInfo[] symbolInfos)
	{
		this.symbolInfos = symbolInfos;
	}

	@JsonProperty("accounts")
	public AccountSettings[] getAccounts()
	{
		return accounts;
	}

	@JsonProperty("accounts")
	public void setAccounts(AccountSettings[] accounts)
	{
		this.accounts = accounts;
	}

	public AccountSettings getAccount(String name)
	{
		return Arrays.stream(accounts).filter(a -> a.getName().equals(name)).findAny().orElse(null);
	}

	public double getAnnualGrowth(String symbol)
	{
		return Arrays.stream(symbolInfos).filter(s -> s.getSymbol().equals(symbol)).mapToDouble(s -> s.getAnnualGrowthPct()).findAny()
				.orElse(defaultAnnualGrowthPct) / 100;
	}

	public boolean excludeOption(String symbol)
	{
		return Arrays.asList(optionsExclude).contains(symbol);
	}

	public boolean includeOption(String symbol)
	{
		return Arrays.asList(optionsInclude).contains(symbol);
	}
}
