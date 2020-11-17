package com.roddyaj.vf.strategy;

import com.roddyaj.vf.model.Pair;
import com.roddyaj.vf.model.SymbolData;

public interface Strategy
{
	Pair<Boolean, String> evaluate(SymbolData data);
}
