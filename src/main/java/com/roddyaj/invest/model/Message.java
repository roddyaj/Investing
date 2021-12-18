package com.roddyaj.invest.model;

import java.util.Collection;
import java.util.List;

import com.roddyaj.invest.html.DataFormatter;
import com.roddyaj.invest.html.HtmlUtils;
import com.roddyaj.invest.html.Table.Align;
import com.roddyaj.invest.html.Table.Column;

public record Message(Level level, String text)
{
	public static List<String> toHtml(Collection<? extends Message> messages)
	{
		return new MessageFormatter(messages).toBlock(false).toHtml();
	}

	public enum Level
	{
		INFO, WARN, ERROR
	}

	public static class MessageFormatter extends DataFormatter<Message>
	{
		public MessageFormatter(Collection<? extends Message> messages)
		{
			super("Messages", null, messages);
		}

		@Override
		protected List<Column> getColumns()
		{
			return List.of(new Column("Message", "%s", Align.L));
		}

		@Override
		protected List<Object> toRow(Message message)
		{
			return List.of(color(message.text, message.level));
		}

		private static String color(String s, Level level)
		{
			return HtmlUtils.color(s, level == Level.ERROR ? "red" : level == Level.WARN ? "orange" : "black");
		}
	}
}
