package com.roddyaj.vf.strategy;

import com.roddyaj.vf.model.SymbolData;

public interface Strategy
{
	boolean evaluate(SymbolData data);
}