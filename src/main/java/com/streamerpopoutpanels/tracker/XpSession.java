package com.streamerpopoutpanels.tracker;

import java.util.List;
import lombok.Value;
import net.runelite.api.Skill;

@Value
public class XpSession
{
	long elapsedSeconds;
	long totalGained;
	long totalPerHour;
	/**
	 * Skills with XP gained this session, most gained first.
	 */
	List<SkillGain> skills;

	@Value
	public static class SkillGain
	{
		Skill skill;
		long gained;
		long perHour;
		int level;
		/**
		 * Progress through the current level, 0 to 1.
		 */
		double levelProgress;
		long xpToLevel;
		/**
		 * Estimated seconds until the next level, or -1 if unknown.
		 */
		long secondsToLevel;
	}
}
