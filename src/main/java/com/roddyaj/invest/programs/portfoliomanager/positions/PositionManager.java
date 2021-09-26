package com.roddyaj.invest.programs.portfoliomanager.positions;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.roddyaj.invest.api.model.QuoteRegistry;
import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.model.Action;
import com.roddyaj.invest.model.Input;
import com.roddyaj.invest.model.Message.Level;
import com.roddyaj.invest.model.Order;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.settings.AccountSettings;

public class PositionManager
{
	private final Account account;

	private final AccountSettings accountSettings;

	private final PositionManagerOutput output;

	private final QuoteRegistry quoteRegistry;

	public PositionManager(Input input)
	{
		account = input.getAccount();
		accountSettings = input.getAccount().getAccountSettings();
		output = new PositionManagerOutput();
		quoteRegistry = input.getQuoteRegistry();
	}

	public PositionManagerOutput run()
	{
		if (accountSettings == null)
		{
			output.addMessage(Level.INFO, "Account not found: " + account.getName());
			return output;
		}

		if (!LocalDate.now().equals(account.getDate()))
			output.addMessage(Level.WARN, "Account data is not from today: " + account.getDate());

		// Determine the managed orders
		List<Order> orders = accountSettings.allocationStream().map(this::createOrder).filter(Objects::nonNull).filter(this::allowOrder)
				.sorted((o1, o2) -> Double.compare(o1.getAmount(), o2.getAmount())).collect(Collectors.toList());
		output.addOrders(orders);

		// Handle odd lots
		output.addUnmanagedOrders(new OddLots(account, accountSettings).run());

		return output;
	}

	private Order createOrder(String symbol)
	{
		Order order = null;

		double targetValue = account.getTotalValue() * account.getAllocation(symbol);

		Position position = account.getPosition(symbol);
		if (position != null)
		{
			double delta = targetValue - position.getMarketValue();
			int sharesToBuy = (int)Math.round(delta / position.getPrice());
			order = new Order(symbol, sharesToBuy, position.getPrice(), position);
			order.setOptional(order.getPosition() != null
					&& (order.getQuantity() >= 0 ? order.getPosition().getDayChangePct() > .1 : order.getPosition().getDayChangePct() < -.1));
		}
		else if (targetValue > 0)
		{
			Double price = quoteRegistry.getPrice(symbol);
			if (price != null)
			{
				int sharesToBuy = (int)Math.round(targetValue / price);
				order = new Order(symbol, sharesToBuy, price, null);
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

	private boolean allowOrder(Order order)
	{
		boolean allowOrder = true;
		Position position = order.getPosition();
		if (position != null)
		{
			double minOrderAmount = Math.max(position.getMarketValue() * 0.005, 40);
			if (order.getQuantity() < 0)
				minOrderAmount *= 2;
			allowOrder = Math.abs(order.getAmount()) > minOrderAmount;
		}
		return allowOrder;
	}
}