package com.roddyaj.invest.programs.vf.strategy;

import java.io.IOException;

import com.roddyaj.invest.programs.vf.model.SymbolData;
import com.roddyaj.invest.programs.vf.model.SymbolResult;

public interface Strategy
{
	String getName();

	boolean evaluate(SymbolData data, SymbolResult result) throws IOException;
}
