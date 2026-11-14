package com.streamerpopoutpanels;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single entry in the recent achievements panel. Stored as JSON in the RS profile config.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Achievement
{
	private AchievementType type;
	private String text;
	/**
	 * Name of the {@link net.runelite.api.Skill} for level ups, used to draw the skill icon. Null otherwise.
	 */
	private String skill;
	private long timestamp;
}
