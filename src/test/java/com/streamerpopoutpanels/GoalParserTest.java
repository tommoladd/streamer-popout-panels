package com.streamerpopoutpanels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import net.runelite.api.Experience;
import net.runelite.api.Skill;
import org.junit.Test;

public class GoalParserTest
{
	private static Goal single(String text)
	{
		List<Goal> goals = GoalParser.parse(text);
		return goals.isEmpty() ? null : goals.get(0);
	}

	@Test
	public void levelGoal()
	{
		Goal goal = single("Slayer 99");
		assertEquals(Skill.SLAYER, goal.getSkill());
		assertEquals(Experience.getXpForLevel(99), goal.getTarget());
		assertEquals("Slayer level 99", goal.getLabel());
	}

	@Test
	public void xpGoals()
	{
		assertEquals(50_000_000, single("Fishing 50m").getTarget());
		assertEquals(250_000, single("fishing 250k").getTarget());
		assertEquals(13_034_431, single("Fishing 13,034,431 xp").getTarget());
		assertEquals(500, single("Fishing 500").getTarget());
		assertEquals(200_000_000, single("Fishing 500m").getTarget());
		assertEquals("Fishing 13.5M xp", single("Fishing 13.5m").getLabel());
	}

	@Test
	public void totalLevel()
	{
		Goal goal = single("Total 2000");
		assertTrue(goal.isTotalLevel());
		assertEquals(2000, goal.getTarget());
	}

	@Test
	public void aliasesAndSeparators()
	{
		List<Goal> goals = GoalParser.parse("hp 99, rc 77\nsailing 50");
		assertEquals(3, goals.size());
		assertEquals(Skill.HITPOINTS, goals.get(0).getSkill());
		assertEquals(Skill.RUNECRAFT, goals.get(1).getSkill());
		assertEquals(Skill.SAILING, goals.get(2).getSkill());
	}

	@Test
	public void invalid()
	{
		assertNull(single("Dancing 99"));
		assertNull(single("Slayer"));
		assertNull(single(""));
		assertNull(single("Slayer 1"));
		assertTrue(GoalParser.parse(null).isEmpty());
	}
}
