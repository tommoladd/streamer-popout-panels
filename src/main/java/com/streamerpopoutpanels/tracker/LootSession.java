package com.streamerpopoutpanels.tracker;

import java.awt.image.BufferedImage;
import java.util.List;
import lombok.Value;

@Value
public class LootSession
{
	long totalValue;
	int lootCount;
	/**
	 * Most recent loot, or null if none this session.
	 */
	Loot lastLoot;
	/**
	 * Most valuable single item stacks this session, most valuable first.
	 */
	List<LootItem> bestDrops;

	@Value
	public static class Loot
	{
		String source;
		long value;
		/**
		 * Items in the loot, most valuable first.
		 */
		List<LootItem> items;
	}

	@Value
	public static class LootItem
	{
		BufferedImage image;
		String name;
		int quantity;
		long value;
		String source;
	}
}
