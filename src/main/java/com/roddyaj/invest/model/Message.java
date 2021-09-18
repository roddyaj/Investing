package com.roddyaj.invest.model;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.roddyaj.invest.html.Block;
import com.roddyaj.invest.html.Table;
import com.roddyaj.invest.html.Table.Align;
import com.roddyaj.invest.html.Table.Column;
import com.roddyaj.invest.util.HtmlFormatter;

public class Message
{
	public final Level level;
	public final String text;

	public Message(Level level, String text)
	{
		this.level = level;
		this.text = text;
	}

	public enum Level
	{
		INFO, WARN, ERROR
	}

	public static class MessageFormatter
	{
		public static Block toBlock(Collection<? extends Message> messages)
		{
			Table table = new Table(getColumns(), getRows(messages));
			table.setShowHeader(false);
			return new Block("Messages", null, table);
		}

		private static List<Column> getColumns()
		{
			return List.of(new Column("Message", "%s", Align.L));
		}

		private static List<List<Object>> getRows(Collection<? extends Message> messages)
		{
			return messages.stream().map(MessageFormatter::toRow).collect(Collectors.toList());
		}

		private static List<Object> toRow(Message m)
		{
			return List.of(color(m.text, m.level));
		}

		private static String color(String s, Level level)
		{
			return HtmlFormatter.color(s, level == Level.ERROR ? "red" : level == Level.WARN ? "orange" : "black");
		}
	}
}
