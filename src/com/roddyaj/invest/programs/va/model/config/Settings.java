package com.roddyaj.invest.programs.va.model.config;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Settings
{
	private String defaultDataDir;

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
}
