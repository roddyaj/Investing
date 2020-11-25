package com.roddyaj.vf.strategy;

import java.io.IOException;

import com.roddyaj.vf.model.Report;
import com.roddyaj.vf.model.SymbolData;

public interface Strategy
{
	boolean evaluate(SymbolData data, Report report) throws IOException;
}
