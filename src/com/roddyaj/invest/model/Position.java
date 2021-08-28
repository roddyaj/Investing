package com.roddyaj.invest.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.roddyaj.invest.util.Chart;
import com.roddyaj.invest.util.Chart.HLine;
import com.roddyaj.invest.util.Chart.Point;
import com.roddyaj.invest.util.Chart.Rect;
import com.roddyaj.invest.util.HtmlFormatter;

public class Position implements Comparable<Position>
{
	private final String symbol;
	private final int quantity;
	private final double price;
	private final double marketValue;
	private final SecurityType securityType;
	private final double costBasis;
	private final double dayChangePct;
	private final double gainLossPct;

	private final Option option;

	public Position(String symbol, int quantity, double price, double marketValue, SecurityType securityType, double costBasis, double dayChangePct,
			double gainLossPct, Option option)
	{
		this.symbol = symbol;
		this.quantity = quantity;
		this.price = price;
		this.marketValue = marketValue;
		this.securityType = securityType;
		this.costBasis = costBasis;
		this.dayChangePct = dayChangePct;
		this.gainLossPct = gainLossPct;
		this.option = option;
	}

	public String getSymbol()
	{
		return symbol;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public double getPrice()
	{
		return price;
	}

	public double getMarketValue()
	{
		return marketValue;
//		return quantity * price;
	}

	public SecurityType getSecurityType()
	{
		return securityType;
	}

	public double getCostBasis()
	{
		return costBasis;
	}

	public double getDayChangePct()
	{
		return dayChangePct;
	}

	public double getGainLossPct()
	{
		return gainLossPct;
	}

	public Option getOption()
	{
		return option;
	}

	public double getCostPerShare()
	{
		return costBasis / quantity;
	}

	public boolean isOption()
	{
		return option != null;
	}

	public boolean isCallOption()
	{
		return isOption() && option.getType() == 'C';
	}

	public boolean isPutOption()
	{
		return isOption() && option.getType() == 'P';
	}

	public double getMoneyInPlay()
	{
		return option != null ? quantity * (option.getType() == 'P' ? option.getStrike() : option.getUnderlyingPrice()) * -100 : getMarketValue();
	}

	@Override
	public String toString()
	{
		return symbol + ", quantity=" + quantity + ", price=" + price + ", marketValue=" + getMarketValue() + ", dayChangePct=" + dayChangePct
				+ ", costBasis=" + costBasis + ", option=[" + option + "], moneyInPlay=" + getMoneyInPlay();
	}

	@Override
	public int compareTo(Position o)
	{
		return symbol.compareTo(o.symbol);
	}

	public static class StockHtmlFormatter extends HtmlFormatter<Position>
	{
		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Ticker", "%s", Align.L));
			columns.add(new Column("#", "%d", Align.R));
			columns.add(new Column("MarketValue", "$%.2f", Align.R));
			columns.add(new Column("Cost Basis", "$%.2f", Align.R));
			columns.add(new Column("Day Change", "%.2f%%", Align.R));
			return columns;
		}

		@Override
		protected List<Object> getObjectElements(Position p)
		{
			return List.of(p.symbol, p.quantity, p.getMarketValue(), p.costBasis, p.dayChangePct);
		}
	}

	public static class OptionHtmlFormatter extends HtmlFormatter<Position>
	{
		private static final String URL = "https://client.schwab.com/Areas/Trade/Allinone/index.aspx#symbol/";

		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Ticker", "%s", Align.L));
			columns.add(new Column("#", "%d", Align.R));
			columns.add(new Column("", "%s", Align.C));
			columns.add(new Column("Expiration", "%s", Align.L));
			columns.add(new Column("DTE", "%d", Align.R));
			columns.add(new Column("Strike", "%.2f", Align.R));
			columns.add(new Column("Price", "%.2f", Align.R));
//			columns.add(new Column("Cost", "%.2f", Align.R));
			columns.add(new Column("Chart", "%s", Align.C));
//			columns.add(new Column("", "%s", Align.C));
			return columns;
		}

		@Override
		protected List<Object> getObjectElements(Position p)
		{
			String link = toLink(URL + p.option.toOccString().replace(' ', '+'), p.symbol);
			int dte = p.option.getDteCurrent();
//			Double underlyingCostPerShare = p.option.underlying != null ? p.option.underlying.getCostPerShare() : null;
//			String moneyText = "OTM".equals(p.option.getMoney()) ? "" : dte < 5 ? "**" : "*";

			return Arrays.asList(link, p.quantity, p.option.getType(), p.option.getExpiryDate(), dte, p.option.getStrike(),
					p.option.getUnderlyingPrice(), getSvgChart(p.option));
		}

		private static String getSvgChart(Option option)
		{
			if (option.getInitialDate() == null)
				return "";

			long totalDays = ChronoUnit.DAYS.between(option.getInitialDate(), option.getExpiryDate());
			long now = ChronoUnit.DAYS.between(option.getInitialDate(), LocalDate.now());
//			double[] prices = new double[] { option.getStrike(), option.getUnderlyingPrice() };
			double minPrice = option.getStrike() * .8; // Arrays.stream(prices).min().orElse(0);
			double maxPrice = option.getStrike() * 1.2; // Arrays.stream(prices).max().orElse(0);
			boolean otm = "OTM".equals(option.getMoney());

			Chart chart = new Chart(0, totalDays, minPrice, maxPrice);
			if (option.getType() == 'C')
			{
				chart.getRectangles().add(new Rect(0, maxPrice, totalDays, maxPrice - option.getStrike(), "#E0E0FF"));
				if (option.getUnderlying() != null)
					chart.getHLines().add(new HLine(option.getUnderlying().getCostPerShare(), "green"));
			}
			else if (option.getType() == 'P')
			{
				chart.getRectangles().add(new Rect(0, option.getStrike(), totalDays, option.getStrike() - minPrice, "#E0E0FF"));
			}
//			chart.getHLines().add(new HLine(option.getStrike(), "blue"));
			chart.getPoints().add(new Point(now, option.getUnderlyingPrice(), otm ? "black" : "red"));
			return chart.toSvg(16, 28);
		}
	}
}
