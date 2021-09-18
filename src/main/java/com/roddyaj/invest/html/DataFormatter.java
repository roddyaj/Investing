package com.roddyaj.invest.html;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.roddyaj.invest.html.Table.Column;

public abstract class DataFormatter<T>
{
	protected final String title;
	protected final String info;
	protected final Collection<? extends T> records;

	public DataFormatter(String title, String info, Collection<? extends T> records)
	{
		this.title = title;
		this.info = info;
		this.records = records;
	}

	public Block toBlock()
	{
		Table table = new Table(getColumns(), getRows(records));
		return new Block(title, info, table);
	}

	public List<String> toHtml()
	{
		return toBlock().toHtml();
	}

	protected abstract List<Column> getColumns();

	protected abstract List<Object> toRow(T record);

	protected List<List<Object>> getRows(Collection<? extends T> records)
	{
		return records.stream().map(this::toRow).collect(Collectors.toList());
	}
}
