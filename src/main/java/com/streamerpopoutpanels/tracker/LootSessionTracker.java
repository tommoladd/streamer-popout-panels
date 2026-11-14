package com.streamerpopoutpanels.tracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.ItemComposition;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.http.api.loottracker.LootRecordType;

/**
 * Tracks the value of loot received this session. All methods run on the client thread.
 */
public class LootSessionTracker extends SnapshotPublisher<LootSession>
{
	private static final int BEST_DROPS = 5;
	private static final Comparator<LootSession.LootItem> BY_VALUE =
		Comparator.comparingLong(LootSession.LootItem::getValue).reversed();

	private final ItemManager itemManager;

	private long totalValue;
	private int lootCount;
	private LootSession.Loot lastLoot;
	private final List<LootSession.LootItem> bestDrops = new ArrayList<>();

	@Inject
	LootSessionTracker(ItemManager itemManager)
	{
		this.itemManager = itemManager;
	}

	public void reset()
	{
		totalValue = 0;
		lootCount = 0;
		lastLoot = null;
		bestDrops.clear();
		publish();
	}

	/**
	 * NPC kills. Posted by the client itself, so this works without the Loot Tracker plugin.
	 */
	@Subscribe
	public void onNpcLootReceived(NpcLootReceived event)
	{
		String name = event.getNpc().getName();
		addLoot(name != null ? name : "Unknown", event.getItems());
	}

	/**
	 * Chests, clues, raids, pickpocketing and other non-NPC loot. Posted by the Loot Tracker plugin.
	 */
	@Subscribe
	public void onLootReceived(LootReceived event)
	{
		// NPC loot is handled above, and the Loot Tracker re-posts it here too
		if (event.getType() == LootRecordType.NPC || event.getType() == LootRecordType.PLAYER)
		{
			return;
		}
		addLoot(event.getName(), event.getItems());
	}

	private void addLoot(String source, Collection<ItemStack> stacks)
	{
		// Merge stacks of the same item, e.g. multiple bones from one kill
		Map<Integer, Integer> quantities = new LinkedHashMap<>();
		for (ItemStack stack : stacks)
		{
			quantities.merge(stack.getId(), stack.getQuantity(), Integer::sum);
		}

		List<LootSession.LootItem> items = new ArrayList<>();
		long value = 0;
		for (Map.Entry<Integer, Integer> entry : quantities.entrySet())
		{
			int id = entry.getKey();
			int quantity = entry.getValue();
			ItemComposition composition = itemManager.getItemComposition(id);
			long itemValue = itemManager.getItemPrice(id) * (long) quantity;
			value += itemValue;
			items.add(new LootSession.LootItem(
				itemManager.getImage(id, quantity, quantity > 1 || composition.isStackable()),
				composition.getName(), quantity, itemValue, source));
		}
		items.sort(BY_VALUE);

		totalValue += value;
		lootCount++;
		lastLoot = new LootSession.Loot(source, value, items);

		for (LootSession.LootItem item : items)
		{
			if (item.getValue() > 0)
			{
				bestDrops.add(item);
			}
		}
		bestDrops.sort(BY_VALUE);
		if (bestDrops.size() > BEST_DROPS)
		{
			bestDrops.subList(BEST_DROPS, bestDrops.size()).clear();
		}

		publish();
	}

	private void publish()
	{
		publish(new LootSession(totalValue, lootCount, lastLoot, new ArrayList<>(bestDrops)));
	}
}
