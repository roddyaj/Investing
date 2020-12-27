package com.roddyaj.invest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.roddyaj.invest.commands.ListCommand;
import com.roddyaj.invest.commands.RunCommand;
import com.roddyaj.invest.model.Program;
import com.roddyaj.invest.va.ValueAverager;
import com.roddyaj.invest.vf.ValueFinder;

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
		Path dataDir = Paths.get(System.getProperty("user.home"), ".invest");
		populateMap(programs, List.of(new ValueFinder(dataDir), new ValueAverager(dataDir)));
		populateMap(commands, List.of(new ListCommand(programs), new RunCommand(programs)));
	}

	public void run(String[] args)
	{
		if (args.length == 0)
		{
			System.err.println("No command specified");
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
