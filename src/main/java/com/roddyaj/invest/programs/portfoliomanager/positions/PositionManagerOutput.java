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

	public List<Order> getOrders()
	{
		return orders;
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
		List<Order> favorableOrders = orders.stream().filter(o -> !o.isOptional()).collect(Collectors.toList());
		List<Order> unfavorableOrders = orders.stream().filter(o -> o.isOptional()).collect(Collectors.toList());
		lines.addAll(new Order.OrderFormatter("Orders", "(Managed positions)", favorableOrders).toHtml());
		lines.addAll(new Order.OrderFormatter("Potential Orders", "(Unmanaged positions)", unmanagedOrders).toHtml());
		lines.addAll(new Order.OrderFormatter("Unfavorable Orders", "(Managed positions)", unfavorableOrders).toHtml());
		return lines;
	}
}
