package com.roddyaj.invest.framework;

import java.util.Queue;

public interface Program
{
	default String getName()
	{
		return getClass().getSimpleName();
	}

	void run(Queue<String> args);
}
