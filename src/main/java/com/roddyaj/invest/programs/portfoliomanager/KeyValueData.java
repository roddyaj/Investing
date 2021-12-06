package com.roddyaj.invest.programs.portfoliomanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.roddyaj.invest.html.Block;
import com.roddyaj.invest.html.DataFormatter;
import com.roddyaj.invest.html.Table.Align;
import com.roddyaj.invest.html.Table.Column;
import com.roddyaj.invest.util.Pair;

public class KeyValueData
{
	private final List<Pair<String, Object>> data = new ArrayList<>();

	private final String title;

	public KeyValueData(String title)
	{
		this.title = title;
	}

	public void addData(String key, Object value)
	{
		data.add(new Pair<>(key, value));
	}

	public Block toBlock()
	{
		return new KeyValueFormatter(data, title).toBlock(false);
	}

	private static class KeyValueFormatter extends DataFormatter<Pair<String, Object>>
	{
		public KeyValueFormatter(Collection<? extends Pair<String, Object>> records, String title)
		{
			super(title, null, records);
		}

		@Override
		protected List<Column> getColumns()
		{
			List<Column> columns = new ArrayList<>();
			columns.add(new Column("Key", "%s", Align.L));
			columns.add(new Column("Value", "%s", Align.R));
			return columns;
		}

		@Override
		protected List<Object> toRow(Pair<String, Object> o)
		{
			return List.of(o.left, o.right);
		}
	}
}
