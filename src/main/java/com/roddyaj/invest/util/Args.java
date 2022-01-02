package com.roddyaj.invest.util;

import java.util.Iterator;
import java.util.Queue;

public final class Args
{
	public static boolean isPresent(Queue<String> args, String arg)
	{
		boolean isPresent = false;
		for (Iterator<String> iter = args.iterator(); iter.hasNext();)
		{
			String anArg = iter.next();
			if (anArg.equals(arg))
			{
				isPresent = true;
				iter.remove();
			}
		}
		return isPresent;
	}

	private Args()
	{
	}
}
