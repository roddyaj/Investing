package com.roddyaj.invest.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface Formatter<T>
{
	String getHeader();

	String format(T object);

	default List<String> format(Collection<? extends T> objects)
	{
		List<String> lines = new ArrayList<>(1 + objects.size());
		lines.add(getHeader());
		for (T object : objects)
			lines.add(format(object));
		return lines;
	}
}
