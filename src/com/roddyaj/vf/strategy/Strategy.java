package com.roddyaj.vf.strategy;

import java.io.IOException;

import com.roddyaj.vf.model.SymbolData;
import com.roddyaj.vf.model.SymbolResult;

public interface Strategy
{
	boolean evaluate(SymbolData data, SymbolResult result) throws IOException;
}
