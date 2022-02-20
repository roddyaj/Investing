package com.roddyaj.invest.programs.portfoliomanager.positions;

import java.util.List;

import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.model.CompletePosition;
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
		List<Order> orders = account.getCompletePositions().stream()
			.filter(p -> !p.getPosition().isOption())
			.filter(this::isOddLot)
			.filter(this::isUntracked)
			.map(this::getOrder)
			.filter(o -> o.quantity() != 0)
			.sorted((o1, o2) -> Double.compare(o2.completePosition().getPosition().getGainLossPct(), o1.completePosition().getPosition().getGainLossPct()))
			.toList();
		// @formatter:on
		return new OddLotsOutput(orders);
	}

	private Order getOrder(CompletePosition completePosition)
	{
		Position position = completePosition.getPosition();
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
		return new Order(position.getSymbol(), quantity, price, completePosition, true);
	}

	private boolean isUntracked(CompletePosition position)
	{
		return account.getAllocation(position.getPosition().getSymbol()) == 0;
	}

	private boolean isOddLot(CompletePosition position)
	{
		int quantity = position.getPosition().getQuantity();
		return quantity > 0 && (quantity % LOT_SIZE) != 0;
	}
}
