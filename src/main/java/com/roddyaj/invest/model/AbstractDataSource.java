package com.roddyaj.invest.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractDataSource implements AccountDataSource
{
	private List<CompletePosition> completePositions;

	@Override
	public List<CompletePosition> getCompletePositions()
	{
		if (completePositions == null)
		{
			List<Position> positions = getPositions();
			Map<String, List<Transaction>> symbolToTransactions = getTransactions().stream().collect(Collectors.groupingBy(Transaction::symbol));
			Map<String, List<OpenOrder>> symbolToOpenOrders = getOpenOrders().stream().collect(Collectors.groupingBy(OpenOrder::symbol));

			completePositions = new ArrayList<>(positions.size());
			for (Position position : positions)
			{
				CompletePosition completePosition = new CompletePosition(position);
				completePosition.getTransactions().addAll(symbolToTransactions.getOrDefault(position.getSymbol(), List.of()));
				completePosition.getOpenOrders().addAll(symbolToOpenOrders.getOrDefault(position.getSymbol(), List.of()));
				completePositions.add(completePosition);
			}
		}
		return completePositions;
	}
}
