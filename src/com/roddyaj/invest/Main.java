package com.roddyaj.invest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.roddyaj.invest.framework.ListCommand;
import com.roddyaj.invest.framework.Program;
import com.roddyaj.invest.framework.RunCommand;
import com.roddyaj.invest.programs.dataroma.Dataroma;
import com.roddyaj.invest.programs.options.Options;
import com.roddyaj.invest.programs.va.ValueAverager;
import com.roddyaj.invest.programs.vf.ValueFinder;

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
		populateMap(programs, new ValueFinder(dataDir), new ValueAverager(dataDir), new Options(), new Dataroma());
		populateMap(commands, new ListCommand(programs), new RunCommand(programs));
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

	private static void populateMap(Map<String, Program> map, Program... items)
	{
		for (Program item : items)
			map.put(item.getName(), item);
	}
}
