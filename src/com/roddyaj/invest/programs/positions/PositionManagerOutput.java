package com.roddyaj.invest.programs.positions;

import java.util.ArrayList;
import java.util.List;

import com.roddyaj.invest.model.Message;
import com.roddyaj.invest.model.Message.Level;
import com.roddyaj.invest.util.HtmlFormatter;

public class PositionManagerOutput
{
	private final String account;

	private final List<Message> messages = new ArrayList<>();

	private final List<Order> orders = new ArrayList<>();

	public PositionManagerOutput(String account)
	{
		this.account = account;
	}

	public void addMessage(Level level, String text)
	{
		messages.add(new Message(level, text));
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

	public List<String> getContent()
	{
		final String title = account + " Orders";
		List<String> lines = new ArrayList<>();
		lines.addAll(new Message.MessageFormatter().toBlock(messages, null));
		lines.addAll(new Order.OrderFormatter().toBlock(orders, title));
		return lines;
	}
}
