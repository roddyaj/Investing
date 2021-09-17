package com.roddyaj.invest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.roddyaj.invest.framework.ListCommand;
import com.roddyaj.invest.framework.Program;
import com.roddyaj.invest.framework.RunCommand;
import com.roddyaj.invest.programs.dataroma.Dataroma;
import com.roddyaj.invest.programs.ladder.Ladder;
import com.roddyaj.invest.programs.lynalden.Lyn;
import com.roddyaj.invest.programs.portfoliomanager.PortfolioManager;

public final class Main
{
	private final Map<String, Program> programs = new HashMap<>();

	private final Map<String, Program> commands = new HashMap<>();

	public static void main(String[] args)
	{
		new Main().run(args);
	}

	public Main()
	{
		List<Program> programList = new ArrayList<>();

		programList.add(new PortfolioManager());
		programList.add(new Dataroma());
		programList.add(new Lyn());
		programList.add(new Ladder());

		populateMap(programs, programList);
		populateMap(commands, List.of(new ListCommand(programs), new RunCommand(programs)));
	}

	public void run(String[] args)
	{
		if (args.length == 0)
		{
			String availableCommands = commands.keySet().stream().sorted().collect(Collectors.joining(", "));
			System.err.println("Available commands: " + availableCommands);
			return;
		}

		String commandName = args[0];
		args = Arrays.copyOfRange(args, 1, args.length);
		Program command = commands.get(commandName);
		if (command != null)
			command.run(args);
		else
			System.err.println(String.format("Command '%s' not found", commandName));
	}

	private static void populateMap(Map<String, Program> map, Collection<? extends Program> items)
	{
		for (Program item : items)
			map.put(item.getName(), item);
	}
}
