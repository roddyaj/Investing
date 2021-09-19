package com.roddyaj.invest.framework;

import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

public class RunCommand implements Program
{
	private final Map<String, Program> programs;

	public RunCommand(Map<String, Program> programs)
	{
		this.programs = programs;
	}

	@Override
	public String getName()
	{
		return "run";
	}

	@Override
	public void run(Queue<String> args)
	{
		if (args.isEmpty())
		{
			String availablePrograms = programs.keySet().stream().sorted().collect(Collectors.joining(", "));
			System.err.println("Available programs: " + availablePrograms);
			return;
		}

		String programName = args.poll();
		Program program = programs.get(programName);
		if (program != null)
			program.run(args);
		else
			System.err.println(String.format("Program '%s' not found", programName));
	}
}
