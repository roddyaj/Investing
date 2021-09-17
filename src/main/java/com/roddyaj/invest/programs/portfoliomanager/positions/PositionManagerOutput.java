package com.roddyaj.invest.programs.portfoliomanager.positions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.roddyaj.invest.model.AbstractOutput;
import com.roddyaj.invest.model.Order;
import com.roddyaj.invest.util.HtmlFormatter;

public class PositionManagerOutput extends AbstractOutput
{
	private final List<Order> orders = new ArrayList<>();

	private final List<Order> unmanagedOrders = new ArrayList<>();

	public void addOrders(Collection<? extends Order> orders)
	{
		this.orders.addAll(orders);
	}

	public void addUnmanagedOrders(Collection<? extends Order> orders)
	{
		this.unmanagedOrders.addAll(orders);
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
		lines.addAll(formatter.toBlock(orders.stream().filter(o -> !o.isOptional()).collect(Collectors.toList()), "Orders", "(Managed positions)"));
		lines.addAll(formatter.toBlock(unmanagedOrders, "Potential Orders", "(Unmanaged positions)"));
		lines.addAll(formatter.toBlock(orders.stream().filter(o -> o.isOptional()).collect(Collectors.toList()), "Unfavorable Orders",
				"(Managed positions)"));
		return lines;
	}
}
