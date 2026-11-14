package com.streamerpopoutpanels;

import lombok.Value;
import net.runelite.api.Skill;

/**
 * A goal from the goals config, e.g. "Slayer 99".
 */
@Value
public class Goal
{
	String label;
	/**
	 * The skill, or null for a total level goal.
	 */
	Skill skill;
	/**
	 * Target XP for skill goals, or target total level for total level goals.
	 */
	long target;

	public boolean isTotalLevel()
	{
		return skill == null;
	}
}
