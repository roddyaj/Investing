package com.roddyaj.invest.va.model.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Settings
{
	private Account[] accounts;

	@JsonProperty("accounts")
	public Account[] getAccounts()
	{
		return accounts;
	}

	@JsonProperty("accounts")
	public void setAccounts(Account[] accounts)
	{
		this.accounts = accounts;
	}
}
