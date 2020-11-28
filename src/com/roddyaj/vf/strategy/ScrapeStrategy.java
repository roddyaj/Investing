package com.roddyaj.vf.strategy;

import java.io.IOException;

import com.roddyaj.vf.model.SymbolData;
import com.roddyaj.vf.model.SymbolResult;

public class ScrapeStrategy implements Strategy
{
	@Override
	public String getName()
	{
		return "Scrape";
	}

	@Override
	public boolean evaluate(SymbolData data, SymbolResult result) throws IOException
	{
		// Get all longer time frame data
		data.getName();
		data.getIncomeStatements();
		data.getBalanceSheets();
		data.getEarnings();
		data.getPriceHistory();

		return true;
	}
}
