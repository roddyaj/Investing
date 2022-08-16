package com.roddyaj.invest.api.schwab;

import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.List;

import com.roddyaj.invest.model.Option;
import com.roddyaj.invest.model.Position;
import com.roddyaj.invest.model.SecurityType;
import com.roddyaj.invest.model.settings.AccountSettings;
import com.roddyaj.invest.util.AppFileUtils;
import com.roddyaj.schwabparse.SchwabPosition;
import com.roddyaj.schwabparse.SchwabPositionsData;
import com.roddyaj.schwabparse.SchwabPositionsReader;

public class SchwabPositionsSource
{
	private final AccountSettings accountSettings;

	private SchwabPositionsData positionsData;

	private List<Position> positions;

	public SchwabPositionsSource(AccountSettings accountSettings)
	{
		this.accountSettings = accountSettings;
	}

	public List<Position> getPositions()
	{
		if (positions == null)
		{
			Path positionsFile = getAccountFile();
			if (positionsFile != null)
			{
				positionsData = new SchwabPositionsReader().read(positionsFile);
				positions = positionsData.positions().stream().map(SchwabPositionsSource::convert).toList();
			}
			else
			{
				positions = List.of();
			}
		}
		return positions;
	}

	public ZonedDateTime getDateTime()
	{
		getPositions();
		return positionsData != null ? positionsData.time() : null;
	}

	public double getBalance()
	{
		return positionsData != null ? positionsData.balance() : 0;
	}

	private Path getAccountFile()
	{
		return AppFileUtils.getAccountFile(accountSettings.getName() + ".*-Positions-.*\\.CSV",
			(p1, p2) -> SchwabPositionsReader.getTime(p2).compareTo(SchwabPositionsReader.getTime(p1)));
	}

	private static Position convert(SchwabPosition position)
	{
		SecurityType securityType = parseSecurityType(position.securityType());

		Option option;
		String symbol;
		if (securityType == SecurityType.OPTION)
		{
			option = SchwabUtils.parseOptionText(position.symbol());
			option.setMoney(position.inTheMoney());
			option.setIntrinsicValue(position.intrinsicValue() != null ? position.intrinsicValue().doubleValue() : 0);
			symbol = option.getSymbol();
		}
		else
		{
			option = null;
			symbol = position.symbol();
		}

		// @formatter:off
		return new Position(
			symbol,
			position.description(),
			position.quantity() != null ? position.quantity().intValue() : 0,
			position.price() != null ? position.price().doubleValue() : 0,
			position.marketValue(),
			securityType,
			position.costBasis() != null ? position.costBasis().doubleValue() : 0,
			position.priceChangePct() != null ? position.priceChangePct().doubleValue() : 0,
			position.gainLossPct() != null ? position.gainLossPct().doubleValue() : 0,
			position.percentOfAccount() != null ? position.percentOfAccount().doubleValue() : 0,
			position.peRatio(),
			position._52WeekLow() != null ? position._52WeekLow().doubleValue() : 0,
			position._52WeekHigh() != null ? position._52WeekHigh().doubleValue() : 0,
			position.dividendYield() != null ? position.dividendYield().doubleValue() : 0,
			option
		);
		// @formatter:on
	}

	private static SecurityType parseSecurityType(String s)
	{
		SecurityType securityType = null;
		if (s != null)
		{
			if (s.equals("Equity"))
				securityType = SecurityType.STOCK;
			else if (s.startsWith("ETF"))
				securityType = SecurityType.ETF;
			else if (s.equals("Option"))
				securityType = SecurityType.OPTION;
			else if (s.startsWith("Cash"))
				securityType = SecurityType.CASH;
		}
		return securityType;
	}
}
