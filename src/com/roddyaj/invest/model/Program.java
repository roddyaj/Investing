package com.roddyaj.invest.model;

public interface Program
{
	default String getName()
	{
		return getClass().getSimpleName();
	}

	void run(String[] args);
}
