package com.roddyaj.invest.vf.strategy;

import java.io.IOException;

import com.roddyaj.invest.vf.model.SymbolData;
import com.roddyaj.invest.vf.model.SymbolResult;

public interface Strategy
{
	String getName();

	boolean evaluate(SymbolData data, SymbolResult result) throws IOException;
}
