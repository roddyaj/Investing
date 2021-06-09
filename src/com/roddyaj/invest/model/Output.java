package com.roddyaj.invest.model;

import java.util.List;

import com.roddyaj.invest.model.Message.Level;

public interface Output
{
	void addMessage(Level level, String text);

	List<Message> getMessages();

	List<String> getContent();
}
