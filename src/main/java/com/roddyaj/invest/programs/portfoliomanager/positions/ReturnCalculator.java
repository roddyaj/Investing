package com.roddyaj.invest.programs.portfoliomanager.positions;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.model.Action;
import com.roddyaj.invest.model.Transaction;
import com.roddyaj.invest.model.settings.AccountSettings;

public class ReturnCalculator
{
	private final Account account;

	private final AccountSettings accountSettings;

	public ReturnCalculator(Account account, AccountSettings accountSettings)
	{
		this.account = account;
		this.accountSettings = accountSettings;
	}

	public double run()
	{
		final LocalDate startDate = LocalDate.of(2022, 1, 1);

		double A = accountSettings.getStartingBalance();
		double B = account.getTotalValue();
		List<Transaction> transfers = account.getTransactions().stream().filter(t -> t.action() == Action.TRANSFER && !t.date().isBefore(startDate))
				.toList();
		double F = transfers.stream().mapToDouble(t -> t.amount()).sum();
		double weightedF = transfers.stream().mapToDouble(t -> getWeight(t, startDate) * t.amount()).sum();
		double R = (B - A - F) / (A + weightedF);
		return R;
	}

	private static double getWeight(Transaction transaction, LocalDate startDate)
	{
		long C = 365;
		long D = ChronoUnit.DAYS.between(startDate, transaction.date());
		double W = D >= 0 ? (C - D) / (double)C : 0;
		return W;
	}
}
