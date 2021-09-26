package com.roddyaj.invest.programs.portfoliomanager.positions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.model.Input;
import com.roddyaj.invest.model.settings.AccountSettings;

class PositionManagerTest
{
	@Test
	void testEmpty()
	{
		AccountSettings accountSettings = new AccountSettings();
		TestDataSource dataSource = new TestDataSource();
		Account account = new Account(accountSettings, dataSource);
		Input input = new Input(account);

		PositionManagerOutput output = new PositionManager(input).run();

		Assertions.assertEquals(0, output.getOrders().size());
	}
}
