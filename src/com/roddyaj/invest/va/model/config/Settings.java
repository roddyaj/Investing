package com.roddyaj.invest.va.model.config;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Settings
{
	private AccountSettings[] accounts;

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
