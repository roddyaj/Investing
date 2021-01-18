package com.roddyaj.invest.util;

import org.json.simple.JSONObject;

public final class JSONUtils
{
	public static double getPercent(JSONObject obj, String key)
	{
		return getDouble(obj, key) / 100;
	}

	public static double getDouble(JSONObject obj, String key)
	{
		Object value = obj.get(key);
		return value instanceof Number ? ((Number)value).doubleValue() : 0;
	}
}
