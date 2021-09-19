package com.roddyaj.invest.programs.positions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.roddyaj.invest.model.Input;
import com.roddyaj.invest.programs.portfoliomanager.positions.PositionManagerCore;
import com.roddyaj.invest.programs.portfoliomanager.positions.PositionManagerOutput;

class PositionManagerCoreTest
{
	@Test
	void test()
	{
		Input input = new Input("testAccount", true);

		PositionManagerOutput positionsOutput = new PositionManagerCore(input).run();

		Assertions.assertTrue(true);
	}
}
