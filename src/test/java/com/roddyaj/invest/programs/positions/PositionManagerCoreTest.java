package com.roddyaj.invest.programs.positions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.roddyaj.invest.model.Input;

class PositionManagerCoreTest
{
	@Test
	void test()
	{
		Input input = new Input("testAccount");

		PositionManagerOutput positionsOutput = new PositionManagerCore(input).run();

		Assertions.assertTrue(true);
	}
}
