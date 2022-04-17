package com.roddyaj.invest.programs.portfoliomanager.positions;

import java.util.List;

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
				.toList();
		// @formatter:on

//		unmanagedPositions.stream().forEach(p -> System.out.println(String.format("        { \"cat\": \"risk.%s\",%s \"%%\":  %.2f },", p.getSymbol(),
//				StringUtils.fill(' ', 5 - p.getSymbol().length()), p.getPercentOfAccount() / .345)));

		if (!unmanagedPositions.isEmpty())
		{
			double untrackedTotal = unmanagedPositions.stream().mapToDouble(Position::getMarketValue).sum();
			double untrackedTarget = account.getAllocation("untracked") * account.getTotalValue();
			double difference = untrackedTotal - untrackedTarget;
			unmanagedPositions.add(0, new Position("Overage", null, 0, 0, difference, null, 0, 0, 0, 0, 0, 0, 0, null));
		}

		return unmanagedPositions;
	}
}
