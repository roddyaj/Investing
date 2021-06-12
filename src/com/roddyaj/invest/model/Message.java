package com.roddyaj.invest.model;

import java.util.List;

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

	public static class MessageFormatter extends HtmlFormatter<Message>
	{
		@Override
		public String getHeader()
		{
			return null;
		}

		@Override
		protected List<Column> getColumns()
		{
			return List.of(new Column("Message", "%s", Align.L));
		}

		@Override
		protected List<Object> getObjectElements(Message o)
		{
			return List.of(color(o.text, o.level));
		}

		private static String color(String s, Level level)
		{
			return color(s, level == Level.ERROR ? "red" : level == Level.WARN ? "orange" : "black");
		}
	}
}
