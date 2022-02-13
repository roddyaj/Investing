package com.roddyaj.invest.util;

import java.util.Arrays;

public final class StringUtils
{
	public static String limit(String s, int length)
	{
		return s.substring(0, Math.min(length, s.length()));
	}

	public static String fill(char c, int length)
	{
		char[] chars = new char[length];
		Arrays.fill(chars, c);
		return new String(chars);
	}

	private StringUtils()
	{
	}
}
