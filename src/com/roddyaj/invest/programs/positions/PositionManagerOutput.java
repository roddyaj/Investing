package com.roddyaj.invest.programs.positions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.roddyaj.invest.model.AbstractOutput;
import com.roddyaj.invest.util.HtmlFormatter;

public class PositionManagerOutput extends AbstractOutput
{
	private final List<Order> orders = new ArrayList<>();

	public void setOrders(List<Order> orders)
	{
		this.orders.addAll(orders);
	}

	@Override
	public String toString()
	{
		return HtmlFormatter.toDocument("Orders", getContent());
	}

	@Override
	public List<String> getContent()
	{
		List<String> lines = new ArrayList<>();
		Order.OrderFormatter formatter = new Order.OrderFormatter();
		lines.addAll(formatter.toBlock(orders.stream().filter(o -> !o.optional).collect(Collectors.toList()), "Orders", null));
		lines.addAll(formatter.toBlock(orders.stream().filter(o -> o.optional).collect(Collectors.toList()), "Optional Orders", null));
		return lines;
	}
}
