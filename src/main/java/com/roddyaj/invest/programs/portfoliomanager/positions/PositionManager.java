package com.roddyaj.invest.programs.portfoliomanager.positions;

import java.util.List;
import java.util.Objects;

import com.roddyaj.invest.api.model.QuoteRegistry;
import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.model.Input;
import com.roddyaj.invest.model.Order;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.settings.AccountSettings;
import com.roddyaj.invest.util.StringUtils;

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
//		logCurrentAllocations();

		List<Order> orders = accountSettings.allocationStream().map(this::createOrder).filter(Objects::nonNull)
			.sorted((o1, o2) -> Double.compare(o1.getAmount(), o2.getAmount())).toList();
		return new PositionManagerOutput(orders, account);
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
			boolean doOrder = quantity != 0 && Math.abs(delta / targetValue) > (isBuy ? 0.005 : 0.01)
				&& Math.abs(quantity * position.getPrice()) >= accountSettings.getMinOrder();
			if (doOrder)
			{
				boolean optional = isBuy ? position.getDayChangePct() > .1 : position.getDayChangePct() < -.1;
//				if (List.of("RSP", "IWD", "XYLD", "VWO", "IEMG", "EFA", "VXUS", "VEA").contains(position.getSymbol()))
//					optional = false;
				order = new Order(symbol, quantity, position.getPrice(), position, optional);
			}
		}
		else if (targetValue > 0.01)
		{
			Double price = quoteRegistry.getPrice(symbol);
			if (price != null)
			{
				int quantity = round(targetValue / price, .75);
				order = new Order(symbol, quantity, price, null, false);
			}
			else
			{
				order = new Order(symbol, 0, targetValue, null, false);
			}
		}

		return order;
	}

	private void logTargetAllocations()
	{
		account.getAllocationMap().entrySet().stream().sorted((a1, a2) -> a2.getValue().compareTo(a1.getValue())).forEach(entry -> {
			String symbol = entry.getKey();
			double percent = entry.getValue();
			if (symbol.length() <= 4)
			{
				System.out.println(String.format("        { \"cat\": \"old.%s\",%s \"%%\":  %.4f },", symbol,
					StringUtils.fill(' ', 4 - symbol.length()), percent * 100));
			}
		});
	}

	private void logCurrentAllocations()
	{
		account.getPositions().stream().sorted((p1, p2) -> Double.compare(p2.getPercentOfAccount(), p1.getPercentOfAccount())).forEach(p -> {
			String symbol = p.getSymbol();
			double percent = p.getPercentOfAccount();
			if (symbol.length() <= 4)
			{
				System.out.println(String.format("        { \"cat\": \"old.%s\",%s \"%%\": %.2f },", symbol,
					StringUtils.fill(' ', 4 - symbol.length()), percent));
			}
		});
	}

	static int round(double value, double cutoff)
	{
		return (int)(value >= 0 ? value + (1 - cutoff) : value - (1 - cutoff));
	}
}