package com.roddyaj.invest.programs.portfoliomanager.options;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.roddyaj.invest.api.model.Quote;
import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.model.Action;
import com.roddyaj.invest.model.Input;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.Transaction;

public class OptionsCore
{
	private final Input input;

	private final Account account;

	private final List<Position> positions;

	private final List<Transaction> historicalOptions;

	private final OptionsOutput output;

	public OptionsCore(Input input)
	{
		this.input = input;
		account = input.getAccount();
		positions = input.getAccount().getPositions();
		historicalOptions = input.getAccount().getTransactions().stream().filter(Transaction::isOption).collect(Collectors.toList());
		output = new OptionsOutput();
	}

	public OptionsOutput run()
	{
		setUp();
		analyzeBuyToClose();
		analyzeCallsToSell();
		analyzePutsToSell();
		availableToTrade();
		currentPositions();
		monthlyIncome();
		return output;
	}

	private void setUp()
	{
		for (Position position : positions)
		{
			if (position.isOption())
			{
				// Set the opening date
				Transaction recentTransaction = historicalOptions.stream()
						.filter(o -> o.getAction() == Action.SELL_TO_OPEN && o.getOption().equals(position.getOption())).findFirst().orElse(null);
				if (recentTransaction != null)
					position.getOption().setInitialDate(recentTransaction.getDate());

				// Set the underlying position if available
				Position underlying = account.getPositions(position.getSymbol()).filter(p -> !p.isOption()).findAny().orElse(null);
				position.getOption().setUnderlying(underlying);
			}
		}
	}

	private void analyzeBuyToClose()
	{
		positions.stream().filter(p -> {
			boolean buyToClose = false;
			if (p.isOption())
			{
				Double optionValueRatio = getOptionValueRatio(p);
				if (optionValueRatio != null)
				{
					buyToClose = optionValueRatio <= .65
							&& (p.isPutOption() || p.getOption().getUnderlyingPrice() > p.getOption().getUnderlying().getCostPerShare());
				}
			}
			return buyToClose;
		}).forEach(output.buyToClose::add);
	}

	private void analyzeCallsToSell()
	{
		for (Position position : positions)
		{
			if (!position.isOption() && position.getQuantity() >= 100 && !input.getSettings().excludeOption(position.getSymbol()))
			{
				int totalCallsSold = positions.stream().filter(p -> p.getSymbol().equals(position.getSymbol()) && p.isCallOption())
						.mapToInt(Position::getQuantity).sum();
				int availableShares = position.getQuantity() + totalCallsSold * 100;
				int availableCalls = (int)Math.floor(availableShares / 100.0);
				boolean isUpAtAll = position.getDayChangePct() > -.1 || position.getGainLossPct() > -.1;
				if (availableCalls > 0 && isUpAtAll)
				{
					CallToSell call = new CallToSell(position, availableCalls);
					call.setOpenOrders(account.getOpenOrders(position.getSymbol(), Action.SELL, 'C'));
					output.callsToSell.add(call);
				}
			}
		}
	}

	private void analyzePutsToSell()
	{
		Set<String> symbols = new HashSet<>();

//		// Get list of CSP candidates based on historical activity
//		symbols.addAll(historicalOptions.stream().map(Transaction::getSymbol)
//				.filter(s -> !input.getSettings().excludeOption(s) && !s.matches(".*\\d.*")).collect(Collectors.toSet()));

//		// Add in other candidates
//		Set<String> optionable = input.information.getOptionableStocks().stream().map(r -> r.symbol).collect(Collectors.toSet());
//		String[] guruCodes = new String[] { "SAM" };
//		Set<String> guruPicks = input.information.getDataromaStocks(guruCodes).stream().map(r -> r.symbol).collect(Collectors.toSet());
//		Set<String> intersection = new HashSet<>(optionable);
//		intersection.retainAll(guruPicks);
//		symbols.addAll(intersection);

		// Add in options from the config
		symbols.addAll(Arrays.asList(input.getSettings().getOptionsInclude()));

		// Create the orders with amount available
		for (String symbol : symbols)
		{
			Quote quote = input.getQuoteRegistry().getQuote(symbol);
			Double price = quote != null ? quote.getPrice() : null;
			Double dayChangePct = quote != null ? quote.getChangePercent() : null;
			boolean isDownForDay = dayChangePct == null || dayChangePct.doubleValue() < .1;

			List<Position> symbolPositions = account.getPositions(symbol).collect(Collectors.toList());
			// Existing position: see if we can sell more put(s)
			if (!symbolPositions.isEmpty())
			{
				Position underlying = symbolPositions.stream().filter(p -> !p.isOption()).findFirst().orElse(null);
				int shareCount = underlying != null ? underlying.getQuantity() : 0;
				int putsSold = symbolPositions.stream().filter(Position::isPutOption).mapToInt(Position::getQuantity).sum();
				double totalInvested = (shareCount + putsSold * -100) * price;
				double available = account.getAccountSettings().getMaxPosition() - totalInvested;
				boolean canSell = (available / (price * 100)) > 0.9; // Hack: using price in place of strike since we don't have strike
				if (canSell && isDownForDay)
				{
					PutToSell put = new PutToSell(symbol, available, price, dayChangePct);
					put.setOpenOrders(account.getOpenOrders(symbol, Action.SELL, 'P'));
					output.putsToSell.add(put);
				}
			}
			// New position
			else
			{
				double available = account.getAccountSettings().getMaxPosition();
				// Hack: using price in place of strike since we don't have strike
				boolean canSell = price == null || (available / (price * 100)) > 0.9;
				if (canSell && isDownForDay)
					output.putsToSell.add(new PutToSell(symbol, available, price, dayChangePct));
			}
		}

		// Calculate historical return on each one
		for (PutToSell put : output.putsToSell)
		{
			put.setAverageReturn(calculateAverageReturn(put.getSymbol(), historicalOptions));
		}
	}

	private void availableToTrade()
	{
		double cashBalance = positions.stream().filter(p -> p.getSymbol().equals("Cash & Cash Investments")).mapToDouble(Position::getMarketValue)
				.findAny().orElse(0);
		double putOnHold = positions.stream().filter(Position::isPutOption).mapToDouble(p -> p.getOption().getStrike() * p.getQuantity() * -100)
				.sum();
		output.availableToTrade = cashBalance - putOnHold;
	}

	private void currentPositions()
	{
		positions.stream().filter(Position::isCallOption).sorted().forEach(output.currentPositions::add);
		positions.stream().filter(Position::isPutOption).sorted().forEach(output.currentPositions::add);
	}

	private void monthlyIncome()
	{
		final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM");
		for (Transaction transaction : historicalOptions)
		{
			if (transaction.getAction() == Action.SELL_TO_OPEN || transaction.getAction() == Action.BUY_TO_CLOSE)
				output.monthToIncome.merge(transaction.getDate().format(format), transaction.getAmount(), Double::sum);
		}
	}

	private static double calculateAverageReturn(String symbol, Collection<? extends Transaction> historicalOptions)
	{
		return historicalOptions.stream().filter(t -> t.getSymbol().equals(symbol) && t.getAction() == Action.SELL_TO_OPEN)
				.collect(Collectors.averagingDouble(t -> t.getAnnualReturn()));
	}

	private static Double getOptionValueRatio(Position position)
	{
		Double optionValueRatio = null;
		Integer dteOriginal = position.getOption().getDteOriginal();
		if (dteOriginal != null)
		{
			double linearValue = ((double)position.getOption().getDteCurrent() / dteOriginal)
					* (position.getCostBasis() + (position.getQuantity() * .65));
			optionValueRatio = position.getMarketValue() / linearValue;
		}
		return optionValueRatio;
	}
}