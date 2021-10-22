package com.roddyaj.invest.programs.portfoliomanager.positions;

import java.util.List;
import java.util.stream.Collectors;

import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.model.Action;
import com.roddyaj.invest.model.Order;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.settings.AccountSettings;

public class OddLots
{
	private static final int LOT_SIZE = 100;

	private final Account account;

	private final AccountSettings accountSettings;

	public OddLots(Account account)
	{
		this.account = account;
		this.accountSettings = account.getAccountSettings();
	}

	public OddLotsOutput run()
	{
		// @formatter:off
		List<Order> orders = account.getPositions().stream()
				.filter(p -> !p.isOption())
				.filter(this::isOddLot)
				.filter(this::isUntracked)
				.map(this::getOrder)
				.filter(o -> o.getQuantity() != 0)
				.peek(o -> o.setOptional(true))
				.peek(o -> o.setOpenOrders(account.getOpenOrders(o.getSymbol(), o.getQuantity() >= 0 ? Action.BUY : Action.SELL, null)))
				.sorted((o1, o2) -> Double.compare(o2.getPosition().getGainLossPct(), o1.getPosition().getGainLossPct()))
				.collect(Collectors.toList());
		// @formatter:on
		return new OddLotsOutput(orders);
	}

	private Order getOrder(Position position)
	{
		int quantity = 0;
		double price = position.getPrice();
		// Sell
		if (position.getGainLossPct() > 3 && position.getDayChangePct() > -.1)
		{
			quantity = -(position.getQuantity() % LOT_SIZE);
		}
		// Buy
		else if (position.getGainLossPct() < -3 && position.getDayChangePct() < .1)
		{
			int roundLotQuantity = LOT_SIZE - position.getQuantity() % LOT_SIZE;
			int fullBuyQuantity = Math.max((int)Math.floor(accountSettings.getMaxPosition() / price - position.getQuantity()), 0);
			quantity = Math.min(roundLotQuantity, fullBuyQuantity);
		}
		return new Order(position.getSymbol(), quantity, price, position);
	}

	private boolean isUntracked(Position position)
	{
		return account.getAllocation(position.getSymbol()) == 0;
	}

	private boolean isOddLot(Position position)
	{
		return position.getQuantity() > 0 && (position.getQuantity() % LOT_SIZE) != 0;
	}
}
