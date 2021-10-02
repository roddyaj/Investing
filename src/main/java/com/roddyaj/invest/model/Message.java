package com.roddyaj.invest.model;

import java.util.Collection;
import java.util.List;

import com.roddyaj.invest.html.Block;
import com.roddyaj.invest.html.DataFormatter;
import com.roddyaj.invest.html.HtmlFormatter;
import com.roddyaj.invest.html.Table;
import com.roddyaj.invest.html.Table.Align;
import com.roddyaj.invest.html.Table.Column;

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

	public static class MessageFormatter extends DataFormatter<Message>
	{
		public MessageFormatter(Collection<? extends Message> records)
		{
			super("Messages", null, records);
		}

		@Override
		public Block toBlock()
		{
			Table table = new Table(getColumns(), getRows(records));
			table.setShowHeader(false);
			return new Block(title, info, table);
		}

		@Override
		protected List<Column> getColumns()
		{
			return List.of(new Column("Message", "%s", Align.L));
		}

		@Override
		protected List<Object> toRow(Message record)
		{
			return List.of(color(record.text, record.level));
		}

		private static String color(String s, Level level)
		{
			return HtmlFormatter.color(s, level == Level.ERROR ? "red" : level == Level.WARN ? "orange" : "black");
		}
	}
}
