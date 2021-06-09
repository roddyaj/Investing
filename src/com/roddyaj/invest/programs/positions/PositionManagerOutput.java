package com.roddyaj.invest.programs.positions;

import java.util.ArrayList;
import java.util.List;

import com.roddyaj.invest.model.AbstractOutput;
import com.roddyaj.invest.util.HtmlFormatter;

public class PositionManagerOutput extends AbstractOutput
{
	private final String account;

	private final List<Order> orders = new ArrayList<>();

	public PositionManagerOutput(String account)
	{
		this.account = account;
	}

	public void setOrders(List<Order> orders)
	{
		this.orders.addAll(orders);
	}

	@Override
	public String toString()
	{
		final String title = account + " Orders";
		return HtmlFormatter.toDocument(title, getContent());
	}

	@Override
	public List<String> getContent()
	{
		final String title = account + " Orders";
		List<String> lines = new ArrayList<>();
		lines.addAll(new Order.OrderFormatter().toBlock(orders, title));
		return lines;
	}
}
