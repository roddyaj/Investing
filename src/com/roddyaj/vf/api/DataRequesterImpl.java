package com.roddyaj.vf.api;

import java.io.IOException;
import java.util.List;

import org.json.simple.JSONObject;

import com.roddyaj.vf.api.alphavantage.AlphaVantageAPI;
import com.roddyaj.vf.model.DateAndDouble;
import com.roddyaj.vf.model.SymbolData;
import com.roddyaj.vf.model.SymbolData.DataRequester;

public class DataRequesterImpl implements DataRequester
{
	private final AlphaVantageAPI avAPI;

	public DataRequesterImpl(JSONObject settings) throws IOException
	{
		avAPI = new AlphaVantageAPI(settings);
	}

	public void requestData(SymbolData data) throws IOException
	{
		avAPI.requestData(data);
	}

	@Override
	public String getName(String symbol) throws IOException
	{
		return avAPI.getName(symbol);
	}

	@Override
	public double getEps(String symbol) throws IOException
	{
		return avAPI.getEps(symbol);
	}

	@Override
	public double getAnalystTargetPrice(String symbol) throws IOException
	{
		return avAPI.getAnalystTargetPrice(symbol);
	}

	@Override
	public List<DateAndDouble> getEarnings(String symbol) throws IOException
	{
		return avAPI.getEarnings(symbol);
	}

	@Override
	public List<DateAndDouble> getPriceHistory(String symbol) throws IOException
	{
		return avAPI.getPriceHistory(symbol);
	}

	@Override
	public double getPrice(String symbol) throws IOException
	{
		return avAPI.getPrice(symbol);
	}
}
