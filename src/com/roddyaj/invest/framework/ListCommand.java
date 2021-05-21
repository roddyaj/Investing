package com.roddyaj.invest.framework;

import java.util.Map;

public class ListCommand implements Program
{
	private final Map<String, Program> programs;

	public ListCommand(Map<String, Program> programs)
	{
		this.programs = programs;
	}

	@Override
	public String getName()
	{
		return "list";
	}

	@Override
	public void run(String[] args)
	{
		System.out.println("\nAvailable Programs:");
		programs.keySet().stream().sorted().forEach(System.out::println);
	}
}
