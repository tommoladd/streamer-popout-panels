package com.streamerpopoutpanels.tracker;

import lombok.Value;

@Value
public class Vitals
{
	public enum HitpointsStatus
	{
		NORMAL,
		POISONED,
		VENOMED,
		DISEASED,
	}

	int hitpoints;
	int maxHitpoints;
	HitpointsStatus status;
	int prayer;
	int maxPrayer;
	/**
	 * Run energy, 0 to 100.
	 */
	int runEnergy;
	boolean staminaActive;
}
