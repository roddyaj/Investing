package com.roddyaj.invest.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class CollectionUtils
{
	public static <T> List<T> join(Collection<? extends T> c1, Collection<? extends T> c2)
	{
		List<T> joined = new ArrayList<>(c1.size() + c2.size());
		joined.addAll(c1);
		joined.addAll(c2);
		return joined;
	}

	public static <T> List<T> join(Collection<? extends T> c, T e)
	{
		List<T> joined = new ArrayList<>(c.size() + 1);
		joined.addAll(c);
		joined.add(e);
		return joined;
	}

	private CollectionUtils()
	{
	}
}
