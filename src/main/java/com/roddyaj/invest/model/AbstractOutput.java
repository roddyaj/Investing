package com.roddyaj.invest.model;

import java.util.ArrayList;
import java.util.List;

import com.roddyaj.invest.model.Message.Level;

public abstract class AbstractOutput implements Output
{
	private final List<Message> messages = new ArrayList<>();

	@Override
	public void addMessage(Level level, String text)
	{
		messages.add(new Message(level, text));
	}

	@Override
	public List<Message> getMessages()
	{
		return messages;
	}
}
