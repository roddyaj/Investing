package com.roddyaj.invest.programs.portfoliomanager.options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.roddyaj.invest.html.Block;
import com.roddyaj.invest.model.AbstractOutput;
import com.roddyaj.invest.model.Position;

public class OptionsOutput extends AbstractOutput
{
	public final List<Position> buyToClose = new ArrayList<>();
	public final List<CallToSell> callsToSell = new ArrayList<>();
	public final List<PutToSell> putsToSell = new ArrayList<>();
	public double availableToTrade;
	public final List<Position> currentPositions = new ArrayList<>();

	public List<Block> getActionsBlocks()
	{
		List<Block> blocks = new ArrayList<>();

		String info = String.format("Frees up $%.0f", buyToClose.stream().filter(Position::isPutOption).mapToDouble(Position::getMoneyInPlay).sum());
		blocks.add(new Position.OptionHtmlFormatter("Buy To Close", info, buyToClose, true).toBlock());

		Collections.sort(callsToSell);
		blocks.add(new CallToSell.CallHtmlFormatter(callsToSell).toBlock());

		Collections.sort(putsToSell);
		blocks.add(new PutToSell.PutHtmlFormatter(putsToSell, availableToTrade).toBlock());

		return blocks;
	}

	public Block getCurrentOptionsBlock()
	{
		long callsCount = currentPositions.stream().filter(Position::isCallOption).count();
		double callsInPlay = currentPositions.stream().filter(Position::isCallOption).mapToDouble(Position::getMoneyInPlay).sum();
		long putsCount = currentPositions.stream().filter(Position::isPutOption).count();
		double putsInPlay = currentPositions.stream().filter(Position::isPutOption).mapToDouble(Position::getMoneyInPlay).sum();
		String info = String.format("C: %d $%.0f &nbsp;P: %d $%.0f", callsCount, callsInPlay, putsCount, putsInPlay);
		return new Position.OptionHtmlFormatter("Current Options", info, currentPositions, false).toBlock();
	}
}
