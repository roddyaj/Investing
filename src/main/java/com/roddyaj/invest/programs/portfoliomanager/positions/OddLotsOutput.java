package com.roddyaj.invest.programs.portfoliomanager.positions;

import java.util.List;

import com.roddyaj.invest.html.Block;
import com.roddyaj.invest.model.AbstractOutput;
import com.roddyaj.invest.model.Order;

public class OddLotsOutput extends AbstractOutput
{
	private final List<Order> orders;

	public OddLotsOutput(List<Order> orders)
	{
		this.orders = orders;
	}

	public List<Order> getOrders()
	{
		return orders;
	}

	public Block getBlock()
	{
		return new Order.OrderFormatter("Potential Orders", "(Unmanaged positions)", orders).toBlock();
	}
}
