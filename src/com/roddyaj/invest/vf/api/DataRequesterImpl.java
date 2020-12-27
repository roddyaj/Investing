package com.roddyaj.invest.vf.api;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.json.simple.JSONObject;

import com.roddyaj.invest.vf.api.alphavantage.AlphaVantageAPI;
import com.roddyaj.invest.vf.model.DateAndDouble;
import com.roddyaj.invest.vf.model.SymbolData.BalanceSheet;
import com.roddyaj.invest.vf.model.SymbolData.DataRequester;
import com.roddyaj.invest.vf.model.SymbolData.IncomeStatement;

public class DataRequesterImpl implements DataRequester
{
	private final AlphaVantageAPI avAPI;

	public DataRequesterImpl(JSONObject settings, Path dataDir) throws IOException
	{
		avAPI = new AlphaVantageAPI(settings, dataDir);
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
	public List<IncomeStatement> getIncomeStatements(String symbol) throws IOException
	{
		return avAPI.getIncomeStatements(symbol);
	}

	@Override
	public List<BalanceSheet> getBalanceSheets(String symbol) throws IOException
	{
		return avAPI.getBalanceSheets(symbol);
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
