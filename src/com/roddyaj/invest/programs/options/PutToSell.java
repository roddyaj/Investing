package com.roddyaj.invest.programs.options;

public class PutToSell implements Comparable<PutToSell>
{
	public final String symbol;
	public final double availableAmount;
	public double averageReturn;

	public PutToSell(String symbol, double availableAmount)
	{
		this.symbol = symbol;
		this.availableAmount = availableAmount;
	}

	@Override
	public String toString()
	{
		return String.format("%-4s $%4.0f available (%3.0f%% return)", symbol, availableAmount, averageReturn);
	}

	@Override
	public int compareTo(PutToSell o)
	{
		return Double.compare(o.averageReturn, averageReturn);
	}
}
