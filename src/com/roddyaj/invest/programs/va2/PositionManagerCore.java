package com.roddyaj.invest.programs.va2;

import com.roddyaj.invest.model.Input;
import com.roddyaj.invest.model.settings.AccountSettings;

public class PositionManagerCore
{
	private final Input input;

	private final PositionManagerOutput output;

	public PositionManagerCore(Input input)
	{
		this.input = input;
		output = new PositionManagerOutput(input.account.getName());
	}

	public PositionManagerOutput run()
	{
		final AccountSettings accountSettings = input.account.getAccountSettings();
		double untrackedTotal = input.account.getPositions().stream().filter(p -> !accountSettings.hasAllocation(p.symbol) && p.quantity > 0)
				.mapToDouble(p -> p.marketValue).sum();
//		double untrackedPercent = untrackedTotal / account.getTotalValue();

		output.blah = untrackedTotal;

		return output;
	}
}