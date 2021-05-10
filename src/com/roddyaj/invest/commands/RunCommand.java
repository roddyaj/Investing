package com.roddyaj.invest.commands;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.roddyaj.invest.model.Program;

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
	public void run(String[] args)
	{
		if (args.length == 0)
		{
			String availablePrograms = programs.keySet().stream().sorted().collect(Collectors.joining(", "));
			System.err.println("Available programs: " + availablePrograms);
			return;
		}

		String programName = args[0];
		args = Arrays.copyOfRange(args, 1, args.length);
		Program program = programs.get(programName);
		if (program != null)
			program.run(args);
		else
			System.err.println(String.format("Program '%s' not found", programName));
	}
}
