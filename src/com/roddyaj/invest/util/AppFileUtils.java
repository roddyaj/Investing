package com.roddyaj.invest.util;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AppFileUtils
{
	public static final Path SETTINGS_DIR = Paths.get(System.getProperty("user.home"), ".invest");
	public static final Path INPUT_DIR = Paths.get(System.getProperty("user.home"), "Downloads");
	public static final Path OUTPUT_DIR = Paths.get(System.getProperty("user.home"), "Documents");

	private static final Pattern FILE_PATTERN = Pattern.compile("(.+?)(-Positions-|_Transactions_)([-\\d]+).CSV");

	public static Path getAccountFile(String account, FileType type)
	{
		String pattern = null;
		if (type == FileType.POSITIONS)
			pattern = account + "-Positions-.*\\.CSV";
		else if (type == FileType.TRANSACTIONS)
			pattern = account + "_Transactions_.*\\.CSV";

		Path file = null;
		if (pattern != null)
			file = FileUtils.list(INPUT_DIR, pattern).sorted(AppFileUtils::compare).findFirst().orElse(null);
		return file;
	}

	public static String getFullAccountName(String accountShorthand)
	{
		String accountName = null;
		String pattern = ".*" + accountShorthand + ".*-Positions-.*\\.CSV";
		Path path = FileUtils.list(INPUT_DIR, pattern).sorted(AppFileUtils::compare).findFirst().orElse(null);
		if (path != null)
		{
			Matcher m = FILE_PATTERN.matcher(path.getFileName().toString());
			if (m.find())
				accountName = m.group(1);
		}
		return accountName;
	}

	public static void showHtml(String html, String fileName)
	{
		Path path = Paths.get(OUTPUT_DIR.toString(), fileName);
		try
		{
			Files.writeString(path, html);
			Desktop.getDesktop().browse(path.toUri());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static int compare(Path p1, Path p2)
	{
		return getTime(p2).compareTo(getTime(p1));
	}

	private static String getTime(Path path)
	{
		String timeString = null;
		Matcher m = FILE_PATTERN.matcher(path.getFileName().toString());
		if (m.find())
			timeString = m.group(3).replace("-", "");
		return timeString;
	}

	public enum FileType
	{
		POSITIONS, TRANSACTIONS
	};
}
