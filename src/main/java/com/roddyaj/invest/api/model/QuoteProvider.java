package com.roddyaj.invest.api.model;

import java.io.IOException;

public interface QuoteProvider
{
	Quote getQuote(String symbol) throws IOException;
}
