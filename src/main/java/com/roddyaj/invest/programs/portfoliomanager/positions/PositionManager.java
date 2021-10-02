package com.roddyaj.invest.programs.portfoliomanager.positions;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.roddyaj.invest.api.model.QuoteRegistry;
import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.model.Action;
import com.roddyaj.invest.model.Input;
import com.roddyaj.invest.model.Order;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.settings.AccountSettings;

public class PositionManager
{
	private final Account account;

	private final AccountSettings accountSettings;

	private final QuoteRegistry quoteRegistry;

	public PositionManager(Input input)
	{
		account = input.getAccount();
		accountSettings = input.getAccount().getAccountSettings();
		quoteRegistry = input.getQuoteRegistry();
	}

	public PositionManagerOutput run()
	{
		List<Order> orders = accountSettings.allocationStream().map(this::createOrder).filter(Objects::nonNull)
				.sorted((o1, o2) -> Double.compare(o1.getAmount(), o2.getAmount())).collect(Collectors.toList());
		return new PositionManagerOutput(orders);
	}

	private Order createOrder(String symbol)
	{
		Order order = null;

		double targetValue = account.getTotalValue() * account.getAllocation(symbol);

		Position position = account.getPosition(symbol);
		if (position != null)
		{
			double delta = targetValue - position.getMarketValue();
			int quantity = round(delta / position.getPrice(), .75);

			boolean isBuy = quantity > 0;
			boolean doOrder = quantity != 0 && Math.abs(delta / targetValue) > (isBuy ? 0.005 : 0.01);
			if (doOrder)
			{
				order = new Order(symbol, quantity, position.getPrice(), position);
				order.setOptional(isBuy ? position.getDayChangePct() > .1 : position.getDayChangePct() < -.1);
			}
		}
		else if (targetValue > 0)
		{
			Double price = quoteRegistry.getPrice(symbol);
			if (price != null)
			{
				int quantity = round(targetValue / price, .75);
				order = new Order(symbol, quantity, price, null);
			}
			else
			{
				order = new Order(symbol, 0, targetValue, null);
			}
		}

		if (order != null)
			order.setOpenOrders(account.getOpenOrders(symbol, order.getQuantity() >= 0 ? Action.BUY : Action.SELL, null));

		return order;
	}

	static int round(double value, double cutoff)
	{
		return (int)(value >= 0 ? value + (1 - cutoff) : value - (1 - cutoff));
	}
}