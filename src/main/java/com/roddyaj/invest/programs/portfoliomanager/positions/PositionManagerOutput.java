package com.roddyaj.invest.programs.portfoliomanager.positions;

import java.util.ArrayList;
import java.util.List;

import com.roddyaj.invest.html.Block;
import com.roddyaj.invest.model.AbstractOutput;
import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.model.Order;

public class PositionManagerOutput extends AbstractOutput
{
	private final List<Order> orders;
	private final Account account;

	public PositionManagerOutput(List<Order> orders, Account account)
	{
		this.orders = orders;
		this.account = account;
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
		blocks.add(new Order.OrderFormatter("Orders", "(Managed positions)", favorableOrders, account).toBlock());
		blocks.add(new Order.OrderFormatter("Unfavorable Orders", "(Managed positions)", unfavorableOrders, account).toBlock());
		return blocks;
	}
}
