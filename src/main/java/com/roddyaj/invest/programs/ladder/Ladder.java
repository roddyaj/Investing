package com.roddyaj.invest.programs.ladder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.roddyaj.invest.framework.Program;
import com.roddyaj.invest.html.HtmlUtils;
import com.roddyaj.invest.model.Order;
import com.roddyaj.invest.util.AppFileUtils;

public class Ladder implements Program
{
	// For running in IDE
	public static void main(String[] args)
	{
		new Ladder().run(new LinkedList<>());
	}

	public static final Function<Integer, Double> POWER_OF_2 = i -> Math.pow(2, i);
	public static final Function<Integer, Double> FLAT = i -> 1.;
	public static final Function<Integer, Double> SLOPE_1 = i -> i + 1.;

	@Override
	public void run(Queue<String> args)
	{
		if (args.size() < 5)
		{
			System.out.println("Args: symbol, shareCount, minPrice, maxPrice, numPoints");
			return;
		}

		String symbol = args.poll();
		int shareCount = Integer.parseInt(args.poll());
		double minPrice = Double.parseDouble(args.poll());
		double maxPrice = Double.parseDouble(args.poll());
		int numPoints = Integer.parseInt(args.poll());

		LadderSettings settings = new LadderSettings(symbol, shareCount, minPrice, maxPrice, numPoints, SLOPE_1);
		List<LadderOrder> orders = run(settings);

//		orders.forEach(System.out::println);
		System.out.println("Total shares: " + orders.stream().mapToInt(o -> o.shareCount).sum());

		showHtml(orders);
	}

	public List<LadderOrder> run(LadderSettings settings)
	{
		List<LadderOrder> orders = new ArrayList<>();

		double totalAmount = 0;
		for (int p = 0; p < settings.numPoints; p++)
			totalAmount += settings.getAmount(p);

		double price = settings.maxPrice;
		final double step = settings.getStep();
		for (int p = 0; p < settings.numPoints; p++)
		{
			double numShares = (settings.getAmount(p) / totalAmount) * settings.shareCount;
			int roundedNumShares = (int)Math.round(numShares);

//			System.out.println(price + " " + numShares + " " + roundedNumShares);
			orders.add(new LadderOrder(settings.symbol, price, roundedNumShares));

			price -= step;
		}

		return orders;
	}

	private void showHtml(Collection<? extends LadderOrder> orders)
	{
		List<String> lines = new ArrayList<>();
		List<Order> actualOrders = orders.stream().map(o -> new Order(o.symbol, o.shareCount, o.price, null, false)).collect(Collectors.toList());
		lines.addAll(new Order.OrderFormatter("Orders", null, actualOrders, null).toHtml());

		String html = HtmlUtils.toDocument("Ladder Orders", lines);

		AppFileUtils.showHtml(html, "LadderOrders.html");
	}

	public static class LadderSettings
	{
		public final String symbol;
		public final int shareCount;
		public final double minPrice;
		public final double maxPrice;
		public final int numPoints;
		private final Function<Integer, Double> stepToAmount;

		public LadderSettings(String symbol, int shareCount, double minPrice, double maxPrice, int numPoints, Function<Integer, Double> stepToAmount)
		{
			this.symbol = symbol;
			this.shareCount = shareCount;
			this.minPrice = minPrice;
			this.maxPrice = maxPrice;
			this.numPoints = numPoints;
			this.stepToAmount = stepToAmount;
		}

		public double getStep()
		{
			return (maxPrice - minPrice) / (numPoints - 1);
		}

		public double getAmount(int point)
		{
			return stepToAmount.apply(point);
		}

		@Override
		public String toString()
		{
			return "LadderSettings [symbol=" + symbol + ", shareCount=" + shareCount + ", minPrice=" + minPrice + ", maxPrice=" + maxPrice
					+ ", numPoints=" + numPoints + ", stepToAmount=" + stepToAmount + "]";
		}
	}

	public static class LadderOrder
	{
		public final String symbol;
		public final double price;
		public final int shareCount;

		public LadderOrder(String symbol, double price, int shareCount)
		{
			this.symbol = symbol;
			this.price = price;
			this.shareCount = shareCount;
		}

		@Override
		public String toString()
		{
			return "LadderOrder [symbol=" + symbol + ", price=" + price + ", shareCount=" + shareCount + "]";
		}
	}
}
