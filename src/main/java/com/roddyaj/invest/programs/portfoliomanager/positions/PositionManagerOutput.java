package com.roddyaj.invest.programs.portfoliomanager.positions;

import java.util.ArrayList;
import java.util.List;

import com.roddyaj.invest.html.Block;
import com.roddyaj.invest.model.AbstractOutput;
import com.roddyaj.invest.model.Order;

public class PositionManagerOutput extends AbstractOutput
{
	private final List<Order> orders;

	public PositionManagerOutput(List<Order> orders)
	{
		this.orders = orders;
	}

	public List<Order> getOrders()
	{
		return orders;
	}

	public List<Block> getBlocks()
	{
		List<Block> blocks = new ArrayList<>();
		List<Order> favorableOrders = orders.stream().filter(o -> !o.optional()).toList();
		List<Order> unfavorableOrders = orders.stream().filter(o -> o.optional()).toList();
		blocks.add(new Order.OrderFormatter("Orders", "(Managed positions)", favorableOrders).toBlock());
		blocks.add(new Order.OrderFormatter("Unfavorable Orders", "(Managed positions)", unfavorableOrders).toBlock());
		return blocks;
	}
}
