package com.roddyaj.invest.programs.portfoliomanager.positions;

import java.util.List;

import com.roddyaj.invest.html.Block;
import com.roddyaj.invest.model.AbstractOutput;
import com.roddyaj.invest.model.Account;
import com.roddyaj.invest.model.Order;

public class OddLotsOutput extends AbstractOutput
{
	private final List<Order> orders;
	private final Account account;

	public OddLotsOutput(List<Order> orders, Account account)
	{
		this.orders = orders;
		this.account = account;
	}

	public List<Order> getOrders()
	{
		return orders;
	}

	public Block getBlock()
	{
		return new Order.OrderFormatter("Potential Orders", "(Unmanaged positions)", orders, account).toBlock();
	}
}
