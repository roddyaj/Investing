package com.roddyaj.invest.schwab;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVRecord;

import com.roddyaj.invest.model.Option;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.SecurityType;
import com.roddyaj.invest.util.AppFileUtils;
import com.roddyaj.invest.util.FileUtils;
import com.roddyaj.invest.util.StringUtils;

public class SchwabPositionsSource
{
	private static final String SYMBOL = "Symbol";
//	private static final String DESCRIPTION = "Description";
	private static final String QUANTITY = "Quantity";
	private static final String PRICE = "Price";
//	private static final String PRICE_CHANGE_$ = "Price Change $";
//	private static final String PRICE_CHANGE_PERCENT = "Price Change %";
	private static final String MARKET_VALUE = "Market Value";
//	private static final String DAY_CHANGE_$ = "Day Change $";
	private static final String DAY_CHANGE_PERCENT = "Day Change %";
	private static final String COST_BASIS = "Cost Basis";
//	private static final String GAIN_LOSS_$ = "Gain/Loss $";
	private static final String GAIN_LOSS_PERCENT = "Gain/Loss %";
//	private static final String REINVEST_DIVIDENDS = "Reinvest Dividends?";
//	private static final String CAPITAL_GAINS = "Capital Gains?";
//	private static final String PERCENT_OF_ACCOUNT = "% Of Account";
//	private static final String DIVIDEND_YIELD = "Dividend Yield";
//	private static final String LAST_DIVIDEND = "Last Dividend";
//	private static final String EX_DIVIDEND_DATE = "Ex-Dividend Date";
//	private static final String P_E_RATIO = "P/E Ratio";
//	private static final String _52_WEEK_LOW = "52 Week Low";
//	private static final String _52_WEEK_HIGH = "52 Week High";
//	private static final String VOLUME = "Volume";
	private static final String INTRINSIC_VALUE = "Intrinsic Value";
	private static final String IN_THE_MONEY = "In The Money";
	private static final String SECURITY_TYPE = "Security Type";

	private final String name;

	private List<Position> positions;

	private LocalDate date;

	public SchwabPositionsSource(String name)
	{
		this.name = name;
	}

	public List<Position> getPositions()
	{
		if (positions == null)
		{
			Path positionsFile = AppFileUtils.getAccountFile(name + "-Positions-.*\\.CSV");
			if (positionsFile != null)
			{
				positions = FileUtils.readCsv(positionsFile, 2).stream().map(SchwabPositionsSource::convert).collect(Collectors.toList());

				final Pattern datePattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
				Matcher matcher = datePattern.matcher(positionsFile.getFileName().toString());
				if (matcher.find())
					date = LocalDate.parse(matcher.group(1));
			}
			else
			{
				positions = List.of();
			}
		}
		return positions;
	}

	public LocalDate getDate()
	{
		return date;
	}

	private static Position convert(CSVRecord record)
	{
		String symbolOrOption = record.get(SYMBOL);
		int quantity = StringUtils.parseInt(record.get(QUANTITY));
		double price = StringUtils.parsePrice(record.get(PRICE));
		double marketValue = StringUtils.parsePrice(record.get(MARKET_VALUE));
		double dayChangePct = StringUtils.parsePercent(record.get(DAY_CHANGE_PERCENT));
		double costBasis = StringUtils.parsePrice(record.get(COST_BASIS));
		double gainLossPct = StringUtils.parsePercent(record.get(GAIN_LOSS_PERCENT));
		double intrinsicValue = record.isSet(INTRINSIC_VALUE) ? StringUtils.parseDouble(record.get(INTRINSIC_VALUE)) : 0;
		String money = record.isSet(IN_THE_MONEY) ? record.get(IN_THE_MONEY) : null;
		SecurityType securityType = record.isSet(SECURITY_TYPE) ? parseSecurityType(record.get(SECURITY_TYPE)) : null;

		Option option;
		String symbol;
		if (securityType == SecurityType.OPTION)
		{
			option = SchwabUtils.parseOptionText(symbolOrOption);
			option.setMoney(money);
			option.setIntrinsicValue(intrinsicValue);
			symbol = option.getSymbol();
		}
		else
		{
			option = null;
			symbol = symbolOrOption;
		}

		return new Position(symbol, quantity, price, marketValue, securityType, costBasis, dayChangePct, gainLossPct, option);
	}

	private static SecurityType parseSecurityType(String s)
	{
		SecurityType securityType = null;
		if (s.equals("Equity"))
			securityType = SecurityType.STOCK;
		else if (s.startsWith("ETF"))
			securityType = SecurityType.ETF;
		else if (s.equals("Option"))
			securityType = SecurityType.OPTION;
		else if (s.startsWith("Cash"))
			securityType = SecurityType.CASH;
		return securityType;
	}
}
