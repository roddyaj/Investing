package com.roddyaj.invest.model.settings;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Settings
{
	private String[] optionsExclude;

	private String[] optionsInclude;

	private AccountSettings[] accounts;

	private Api[] apis;

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

	@JsonProperty("apis")
	public Api[] getApis()
	{
		return apis;
	}

	@JsonProperty("apis")
	public void setApis(Api[] apis)
	{
		this.apis = apis;
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

	public boolean excludeOption(String symbol)
	{
		return Arrays.asList(optionsExclude).contains(symbol);
	}

	public boolean includeOption(String symbol)
	{
		return Arrays.asList(optionsInclude).contains(symbol);
	}
}
