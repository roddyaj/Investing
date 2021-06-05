package com.roddyaj.invest.programs.va2;

import java.util.List;

import com.roddyaj.invest.programs.va.model.Order;
import com.roddyaj.invest.util.HtmlFormatter;

public class PositionManagerOutput
{
	private final String account;

	private List<Order> orders;

	public PositionManagerOutput(String account)
	{
		this.account = account;
	}

	public void setOrders(List<Order> orders)
	{
		this.orders = orders;
	}

	@Override
	public String toString()
	{
		final String title = account + " Orders";
		return HtmlFormatter.toDocument(title, getContent());
	}

	public List<String> getContent()
	{
		final String title = account + " Orders";
		return new Order.OrderFormatter().toBlock(orders, title);
	}
}
