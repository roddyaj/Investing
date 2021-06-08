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
		protected List<Column> getColumns()
		{
			return List.of(new Column("Message", "%s", Align.L));
		}

		@Override
		protected List<Object> getObjectElements(Message o)
		{
			return List.of(o.text);
		}

//		private static String color(double d, String format)
//		{
//			return color(String.format(format, d), d >= 0 ? "green" : "red");
//		}
//
//		private static String color(String s, String color)
//		{
//			return "<span style=\"color:" + color + "\">" + s + "</span>";
//		}
	}
}
