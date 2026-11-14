package com.streamerpopoutpanels.tracker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kill count and personal best for one boss or activity. Stored as JSON in the RS profile config.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BossRecord
{
	private String name;
	private int killCount;
	/**
	 * Personal best time as shown in game, e.g. "1:23.40", or null if unknown.
	 */
	private String personalBest;
	private long lastKill;
	/**
	 * When a new personal best was last set, 0 if never seen.
	 */
	private long personalBestSet;

	BossRecord copy()
	{
		return new BossRecord(name, killCount, personalBest, lastKill, personalBestSet);
	}
}
