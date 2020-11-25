package com.roddyaj.vf.strategy;

import com.roddyaj.vf.model.Report;
import com.roddyaj.vf.model.SymbolData;

public class AnalystTargetStrategy implements Strategy
{
	@Override
	public boolean evaluate(SymbolData data, Report report)
	{
		double targetPrice = data.analystTargetPrice * 0.95;
		boolean pass = data.price < targetPrice;
		String message = (pass ? "pass" : "fail") + " " + String.format("%7.2f", targetPrice);
		report.addMessage("Analyst target", message);
		return pass;
	}
}
