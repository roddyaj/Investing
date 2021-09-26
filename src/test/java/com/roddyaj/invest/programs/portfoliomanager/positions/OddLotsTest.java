package com.roddyaj.invest.programs.portfoliomanager.positions;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.model.Order;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.SecurityType;
import com.roddyaj.invest.model.settings.AccountSettings;
import com.roddyaj.invest.model.settings.Allocation;

class OddLotsTest
{
	@Test
	void testEmpty()
	{
		AccountSettings accountSettings = new AccountSettings();
		TestDataSource dataSource = new TestDataSource();

		OddLots oddLots = new OddLots(new Account(accountSettings, dataSource), accountSettings);

		List<Order> orders = oddLots.run();
		Assertions.assertTrue(orders.isEmpty());
	}

	@Test
	void testMain()
	{
		AccountSettings accountSettings = new AccountSettings();
		Allocation[] allocations = new Allocation[1];
		allocations[0] = new Allocation("ABC", 10);
		accountSettings.setAllocations(allocations);
		accountSettings.setMaxPosition(5000);

		TestDataSource dataSource = new TestDataSource();
		dataSource.getPositions().add(newPosition("ABC", 210, 12., 11.));
		dataSource.getPositions().add(newPosition("DEF", 210, 12., 11.));
		dataSource.getPositions().add(newPosition("GHI", 190, 11., 12.));

		OddLots oddLots = new OddLots(new Account(accountSettings, dataSource), accountSettings);

		List<Order> orders = oddLots.run();
		Assertions.assertEquals(2, orders.size());
		Assertions.assertEquals("DEF", orders.get(0).getSymbol());
		Assertions.assertEquals(-10, orders.get(0).getQuantity());
		Assertions.assertEquals("GHI", orders.get(1).getSymbol());
		Assertions.assertEquals(10, orders.get(1).getQuantity());
	}

	private static Position newPosition(String symbol, int quantity, double price, double pricePaid)
	{
		double gainLossPct = 100 * (price / pricePaid - 1);
		double dayChangePct = gainLossPct;
		return new Position(symbol, quantity, price, quantity * price, SecurityType.STOCK, quantity * pricePaid, dayChangePct, gainLossPct, null);
	}
}