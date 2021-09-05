package com.roddyaj.invest.programs.positions;

import java.util.List;
import java.util.stream.Collectors;

import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.model.Action;
import com.roddyaj.invest.model.Order;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.settings.AccountSettings;

public class OddLots
{
	private final Account account;

	private final AccountSettings accountSettings;

	public OddLots(Account account, AccountSettings accountSettings)
	{
		this.account = account;
		this.accountSettings = accountSettings;
	}

	public List<Order> calculateOddLots()
	{
		// @formatter:off
		List<Order> orders = account.getPositions().stream()
			.filter(p -> !p.isOption())
			.filter(this::isOddLot)
			.filter(this::isUntracked)
			.map(this::getOrder)
			.filter(o -> o.getQuantity() != 0)
			.sorted((o1, o2) -> Double.compare(o2.getPosition().getGainLossPct(), o1.getPosition().getGainLossPct()))
			.collect(Collectors.toList());
		// @formatter:on

		orders.forEach(order -> {
			order.setOptional(true);
			order.setOpenOrders(account.getOpenOrders(order.getSymbol(), order.getQuantity() >= 0 ? Action.BUY : Action.SELL, null));
		});

		return orders;
	}

	private Order getOrder(Position position)
	{
		int quantity = 0;
		double price = position.getPrice();
		// Sell
		if (position.getGainLossPct() > 3)
		{
			if (position.getDayChangePct() > -.1)
			{
				quantity = -position.getQuantity();
				price = position.getCostPerShare() * 1.1;
			}
		}
		// Buy
		else if (position.getGainLossPct() < -3)
		{
			if (position.getDayChangePct() < .1)
			{
				int fullBuyQuantity = Math.max((int)Math.floor(accountSettings.getMaxOptionPosition() / price - position.getQuantity()), 0);
				int roundLotQuantity = 100 - position.getQuantity() % 100;
				quantity = Math.min(fullBuyQuantity, roundLotQuantity);
			}
		}
		return new Order(position.getSymbol(), quantity, price, position);
	}

	private boolean isUntracked(Position position)
	{
		return !accountSettings.hasAllocation(position.getSymbol());
	}

	private boolean isOddLot(Position position)
	{
		return position.getQuantity() > 0 && (position.getQuantity() % 100) != 0;
	}

//	// Find odd lots that can be bought
//	untrackedOddPositions.stream()
//		.filter(this::isEvenLotUnderMaxPosition)
//		.filter(p -> p.dayChangePct < .1 || p.gainLossPct < .1)
//		.sorted((p1, p2) -> Double.compare(p1.dayChangePct, p2.dayChangePct))
//		.map(p -> new Order(p.symbol, 100 - p.quantity % 100, p.getPrice(), p))
//		.forEach(orders::add);
//
//	System.out.println("\nPositions that don't have full sell orders:");
//	untrackedOddPositions.stream()
//		.filter(p -> (p.quantity + account.getOpenOrderCount(p.symbol, Action.SELL)) != 0)
//		.forEach(p -> System.out.println(p.symbol));

//	private boolean isEvenLotUnderMaxPosition(Position position)
//	{
//		double totalAmount = (position.quantity / 100 + 1) * 100 * position.price;
//		return totalAmount < accountSettings.getMaxOptionPosition();
//	}
}
