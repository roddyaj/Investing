package com.roddyaj.invest.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Option
{
	private static final DateTimeFormatter OCC_DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");

	private final String symbol;
	private final LocalDate expiryDate;
	private final double strike;
	private final char type;

	private String money;
	private double intrinsicValue;
	private LocalDate initialDate;

	// Note, this can be null
	private Position underlying;

	public Option(String symbol, LocalDate expiryDate, double strike, char type)
	{
		this.symbol = symbol;
		this.expiryDate = expiryDate;
		this.strike = strike;
		this.type = type;
	}

	public void setMoney(String money)
	{
		this.money = money;
	}

	public void setIntrinsicValue(double intrinsicValue)
	{
		this.intrinsicValue = intrinsicValue;
	}

	public void setInitialDate(LocalDate initialDate)
	{
		this.initialDate = initialDate;
	}

	public void setUnderlying(Position underlying)
	{
		this.underlying = underlying;
	}

	public String getSymbol()
	{
		return symbol;
	}

	public LocalDate getExpiryDate()
	{
		return expiryDate;
	}

	public double getStrike()
	{
		return strike;
	}

	public char getType()
	{
		return type;
	}

	public String getMoney()
	{
		return money;
	}

	public double getIntrinsicValue()
	{
		return intrinsicValue;
	}

	public LocalDate getInitialDate()
	{
		return initialDate;
	}

	public Position getUnderlying()
	{
		return underlying;
	}

	public int getDteCurrent()
	{
		return (int)ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
	}

	public int getDteOriginal()
	{
		return initialDate != null ? (int)ChronoUnit.DAYS.between(initialDate, expiryDate) : -1;
	}

	public double getUnderlyingPrice()
	{
		return type == 'P' ? strike - intrinsicValue : strike + intrinsicValue;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(symbol, expiryDate, type, strike);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Option other = (Option)obj;
		return Objects.equals(symbol, other.symbol) && Objects.equals(expiryDate, other.expiryDate) && type == other.type
				&& Double.doubleToLongBits(strike) == Double.doubleToLongBits(other.strike);
	}

	@Override
	public String toString()
	{
		return String.format("%s %s %s %.2f %.2f %s", symbol, type, expiryDate, strike, getUnderlyingPrice(), money);
	}

	public String toOccString()
	{
		StringBuilder b = new StringBuilder(21);
		b.append(String.format("%-6s", symbol));
		b.append(expiryDate.format(OCC_DATE_FORMAT));
		b.append(type);
		b.append(String.format("%08d", Math.round(strike * 1000)));
		return b.toString();
	}
}
