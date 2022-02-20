package com.roddyaj.invest.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.roddyaj.invest.api.schwab.SchwabDataSource;
import com.roddyaj.invest.html.Chart;
import com.roddyaj.invest.html.Chart.HLine;
import com.roddyaj.invest.html.Chart.Point;
import com.roddyaj.invest.html.DataFormatter;
import com.roddyaj.invest.html.HtmlUtils;
import com.roddyaj.invest.html.Table.Align;
import com.roddyaj.invest.html.Table.Column;

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
	private final double percentOfAccount;
	private Lots lots;

	private final Option option;

	public Position(String symbol, int quantity, double price, double marketValue, SecurityType securityType, double costBasis, double dayChangePct,
			double gainLossPct, double percentOfAccount, Option option)
	{
		this.symbol = symbol;
		this.quantity = quantity;
		this.price = price;
		this.marketValue = marketValue;
		this.securityType = securityType;
		this.costBasis = costBasis;
		this.dayChangePct = dayChangePct;
		this.gainLossPct = gainLossPct;
		this.percentOfAccount = percentOfAccount;
		this.option = option;
	}

	public void setLots(Lots lots)
	{
		this.lots = lots;
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

	public double getPercentOfAccount()
	{
		return percentOfAccount;
	}

	public Lots getLots()
	{
		return lots;
	}

	public Double getUnadjustedCostBasis()
	{
		return lots != null ? lots.getCostBasis() : null;
	}

	public Double getUnadjustedCostPerShare()
	{
		return lots != null ? lots.getCostBasis() / quantity : null;
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

	public static class OptionHtmlFormatter extends DataFormatter<Position>
	{
		private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yy/MM/dd");

		private final boolean showOpenOrders;

		public OptionHtmlFormatter(String title, String info, Collection<? extends Position> records, boolean showOpenOrders)
		{
			super(title, info, records);
			this.showOpenOrders = showOpenOrders;
		}

		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Ticker", "%s", Align.L));
			columns.add(new Column("#", "%s", showOpenOrders ? Align.L : Align.R));
			columns.add(new Column("T", "%s", Align.L));
			columns.add(new Column("Expiry", "%s", Align.L));
			columns.add(new Column("", "%s", Align.R));
			columns.add(new Column("Strike", "%.2f", Align.R));
			columns.add(new Column("Price", "%.2f", Align.R));
			columns.add(new Column("Cost", "%.2f", Align.R));
//			columns.add(new Column("U Cost", "%.2f", Align.R));
			columns.add(new Column("", "%s", Align.C));
			return columns;
		}

		@Override
		protected List<Object> toRow(Position p)
		{
			String link = HtmlUtils.toLink(SchwabDataSource.getOptionUrl(p.option.toOccString()), p.symbol);
			String quantityText = String.valueOf(Math.abs(p.quantity));
//			if (showOpenOrders)
//			{
//				List<OpenOrder> openOrders = completePosition.getOpenOrders().stream()
//					.filter(o -> o.option() != null && o.option().getType() == p.getOption().getType() && o.quantity() > 0).toList();
//				quantityText += OpenOrder.getPopupText(p.openOrders);
//			}
			int dte = p.option.getDteCurrent();
			String dteText = "(" + dte + ")";
			Double underlyingCostPerShare = p.option.getUnderlying() != null ? p.option.getUnderlying().getCostPerShare() : null;
//			Double underlyingUCostPerShare = p.option.getUnderlying() != null ? p.option.getUnderlying().getUnadjustedCostPerShare() : null;
			Chart chart = getSvgChart(p.option);
			String chartWithPopup = chart != null ? HtmlUtils.createPopup(chart.toSvg(16, 28), chart.toSvg(64, 114), false) : "";

			return Arrays.asList(link, quantityText, p.option.getType(), p.option.getExpiryDate().format(DATE_FORMAT), dteText, p.option.getStrike(),
					p.option.getUnderlyingPrice(), underlyingCostPerShare, chartWithPopup);
		}

		private static Chart getSvgChart(Option option)
		{
			if (option.getInitialDate() == null)
				return null;

			long totalDays = ChronoUnit.DAYS.between(option.getInitialDate(), option.getExpiryDate());
			long now = ChronoUnit.DAYS.between(option.getInitialDate(), LocalDate.now());
			double minPrice = option.getStrike() * .8;
			double maxPrice = option.getStrike() * 1.2;
			boolean itm = "ITM".equals(option.getMoney());

			Chart chart = new Chart(0, totalDays, minPrice, maxPrice);
			double strike = option.getStrike();
			double price = option.getUnderlyingPrice();

			if (option.getType() == 'C')
			{
				if (option.getUnderlying() != null)
				{
					double cost = option.getUnderlying().getCostPerShare();
					if (strike >= cost)
					{
						chart.addRectangle(strike, maxPrice, color("#04F2", price >= strike));
						chart.addRectangle(cost, strike, "#0F03");
					}
					else
					{
						chart.addRectangle(strike, maxPrice, color("#F002", price >= strike));
					}
				}
				else
				{
					chart.addRectangle(strike, maxPrice, color("#04F2", price >= strike));
				}
			}
			else if (option.getType() == 'P')
			{
				chart.addRectangle(minPrice, strike, color("#04F2", price <= strike));
			}
			chart.addHLine(new HLine(strike, "black"));
			chart.addPoint(new Point(now, price, itm ? "yellow" : "black"));
			return chart;
		}

		private static String color(String color, boolean opaque)
		{
			return opaque ? color.substring(0, 4) : color;
		}
	}
}
