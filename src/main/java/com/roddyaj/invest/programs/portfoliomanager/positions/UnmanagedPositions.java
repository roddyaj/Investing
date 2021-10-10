package com.roddyaj.invest.programs.portfoliomanager.positions;

import java.util.List;
import java.util.stream.Collectors;

import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.model.Position;

public class UnmanagedPositions
{
	private final Account account;

	public UnmanagedPositions(Account account)
	{
		this.account = account;
	}

	public List<Position> run()
	{
		// @formatter:off
		List<Position> unmanagedPositions = account.getPositions().stream()
				.filter(p -> !p.isOption() && p.getQuantity() != 0 && account.getAllocation(p.getSymbol()) == 0)
				.sorted((o1, o2) -> Double.compare(o2.getMarketValue(), o1.getMarketValue()))
				.collect(Collectors.toList());
		// @formatter:on
		double untrackedTotal = unmanagedPositions.stream().mapToDouble(Position::getMarketValue).sum();
		double untrackedTarget = account.getAllocation("untracked") * account.getTotalValue();
		double difference = untrackedTotal - untrackedTarget;
		unmanagedPositions.add(0, new Position("Overage", 0, 0, difference, null, 0, 0, 0, null));
		return unmanagedPositions;
	}
}
