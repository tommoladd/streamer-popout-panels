package com.streamerpopoutpanels.tracker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.runelite.client.util.Text;
import org.junit.Test;

public class BossLogParserTest
{
	private static void assertKc(String message, String name, int count)
	{
		BossLogParser.KillCount kc = BossLogParser.parseKillCount(Text.removeTags(message));
		assertEquals(name, kc.getName());
		assertEquals(count, kc.getCount());
	}

	@Test
	public void killCounts()
	{
		assertKc("Your Zulrah kill count is: <col=ff0000>1,234</col>.", "Zulrah", 1234);
		assertKc("Your completed Chambers of Xeric count is: <col=ff0000>51</col>.", "Chambers of Xeric", 51);
		assertKc("Your completed Tombs of Amascut: Expert Mode count is: <col=ff0000>12</col>.", "Tombs of Amascut: Expert Mode", 12);
		assertKc("Your Barrows chest count is: <col=ff0000>300</col>.", "Barrows", 300);
		assertKc("Your Gauntlet completion count is: <col=ff0000>3</col>.", "Gauntlet", 3);
		assertKc("Your subdued Wintertodt count is: <col=ff0000>500</col>.", "Wintertodt", 500);
		assertKc("You have completed <col=ff0000>123</col> hard Treasure Trails.", "Clue scrolls (hard)", 123);
	}

	@Test
	public void ignoresLapsAndOtherMessages()
	{
		assertNull(BossLogParser.parseKillCount("Your Prifddinas Agility Course lap count is: 12."));
		assertNull(BossLogParser.parseKillCount("Welcome to Old School RuneScape."));
	}

	@Test
	public void personalBests()
	{
		BossLogParser.PersonalBest pb = BossLogParser.parsePersonalBest(
			Text.removeTags("Fight duration: <col=ff0000>1:23.40</col> (new personal best)"));
		assertEquals("1:23.40", pb.getTime());
		assertTrue(pb.isNew());

		pb = BossLogParser.parsePersonalBest(
			Text.removeTags("Fight duration: <col=ff0000>1:30.00</col>. Personal best: 1:10.20"));
		assertEquals("1:10.20", pb.getTime());
		assertFalse(pb.isNew());

		pb = BossLogParser.parsePersonalBest("Team size: 3 players Duration: 1:02:13.20 (new personal best)");
		assertEquals("1:02:13.20", pb.getTime());

		assertNull(BossLogParser.parsePersonalBest("Your Zulrah kill count is: 12."));
	}
}
