package com.roddyaj.invest.framework;

public interface Program
{
	default String getName()
	{
		return getClass().getSimpleName();
	}

	void run(String... args);
}
