package com.roddyaj.invest.vf.api.misc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import com.roddyaj.invest.vf.model.SymbolData;

public final class SymbolReader
{
	public static List<SymbolData> readSymbols(Path file) throws IOException
	{
		return Files.readAllLines(file).stream().filter(s -> !s.contains(".")).map(SymbolData::new).collect(Collectors.toList());
	}
}
