package com.roddyaj.vf.api;

import java.io.IOException;

import org.json.simple.JSONObject;

import com.roddyaj.vf.api.alphavantage.AlphaVantageAPI;
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
	public double getPrice(String symbol) throws IOException
	{
		return avAPI.getPrice(symbol);
	}
}
