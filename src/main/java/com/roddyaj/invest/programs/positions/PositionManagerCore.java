package com.roddyaj.invest.programs.positions;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.model.Action;
import com.roddyaj.invest.model.Input;
import com.roddyaj.invest.model.Message.Level;
import com.roddyaj.invest.model.Order;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.settings.AccountSettings;

public class PositionManagerCore
{
	private final Account account;

	private final AccountSettings accountSettings;

	private final PositionManagerOutput output;

	public PositionManagerCore(Input input)
	{
		account = input.getAccount();
		accountSettings = input.getAccount().getAccountSettings();
		output = new PositionManagerOutput();
	}

	public PositionManagerOutput run()
	{
		if (accountSettings == null)
		{
			output.addMessage(Level.INFO, "Account not found: " + account.getName());
			return output;
		}

		// Create the allocation map
		double untrackedTotal = account.getPositions().stream().filter(p -> p.getQuantity() > 0 && !accountSettings.hasAllocation(p.getSymbol()))
				.mapToDouble(Position::getMarketValue).sum();
		double untrackedPercent = untrackedTotal / account.getTotalValue();
		accountSettings.createMap(untrackedPercent, output);

		if (!LocalDate.now().equals(account.getDate()))
			output.addMessage(Level.WARN, "Account data is not from today: " + account.getDate());

		// Determine the managed orders
		List<Order> orders = accountSettings.allocationStream().map(this::createOrder).filter(Objects::nonNull).filter(this::allowOrder)
				.sorted((o1, o2) -> Double.compare(o1.getAmount(), o2.getAmount())).collect(Collectors.toList());
		output.addOrders(orders);

		// Handle odd lots
		output.addUnmanagedOrders(new OddLots(account, accountSettings).calculateOddLots());

		return output;
	}

	private Order createOrder(String symbol)
	{
		Order order = null;

		double targetValue = account.getTotalValue() * accountSettings.getAllocation(symbol);

		Position position = account.getPosition(symbol);
		if (position != null)
		{
			double delta = targetValue - position.getMarketValue();
			long sharesToBuy = Math.round(delta / position.getPrice());
			order = new Order(symbol, (int)sharesToBuy, position.getPrice(), position);
			order.setOptional(order.getPosition() != null
					&& (order.getQuantity() >= 0 ? order.getPosition().getDayChangePct() > .1 : order.getPosition().getDayChangePct() < -.1));
			order.setOpenOrders(account.getOpenOrders(symbol, order.getQuantity() >= 0 ? Action.BUY : Action.SELL, null));
		}
		else if (targetValue > 0)
		{
			order = new Order(symbol, 0, targetValue, null);
		}

		return order;
	}

	private boolean allowOrder(Order order)
	{
		boolean allowOrder = true;
		Position position = order.getPosition();
		if (position != null)
		{
			double minOrderAmount = Math.max(position.getMarketValue() * 0.005, 50);
			if (order.getQuantity() < 0)
				minOrderAmount *= 2;
			allowOrder = Math.abs(order.getAmount()) > minOrderAmount;
		}
		return allowOrder;
	}
}