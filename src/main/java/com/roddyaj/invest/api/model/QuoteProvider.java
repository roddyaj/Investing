package com.roddyaj.invest.api.model;

import java.io.IOException;

public interface QuoteProvider
{
	String getName();

	Quote getQuote(String symbol) throws IOException;
}
