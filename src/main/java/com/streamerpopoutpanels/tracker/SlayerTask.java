package com.streamerpopoutpanels.tracker;

import lombok.Value;

@Value
public class SlayerTask
{
	/**
	 * Task name, or null if there is no task.
	 */
	String name;
	/**
	 * Konar area, or null.
	 */
	String location;
	int remaining;
	int original;
	int points;
	int streak;
}
