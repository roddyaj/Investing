package com.roddyaj.vf.strategy;

import java.io.IOException;

import com.roddyaj.vf.model.Report;
import com.roddyaj.vf.model.SymbolData;

public class AnalystTargetStrategy implements Strategy
{
	@Override
	public boolean evaluate(SymbolData data, Report report) throws IOException
	{
		double targetPrice = data.analystTargetPrice * 0.95;
		boolean pass = data.getPrice(data.symbol) < targetPrice;
		String message = (pass ? "pass" : "fail") + " " + String.format("%7.2f", targetPrice);
		report.addMessage("Analyst target", message);
		return pass;
	}
}
