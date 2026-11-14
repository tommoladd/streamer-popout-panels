package com.streamerpopoutpanels.tracker;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.streamerpopoutpanels.StreamerPopoutPanelsConfig;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.inject.Inject;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.util.Text;

/**
 * Tracks boss kill counts and personal bests from chat messages. Saved per account.
 * All methods must be called on the client thread.
 */
@Slf4j
public class BossLogTracker extends SnapshotPublisher<List<BossRecord>>
{
	private static final String CONFIG_KEY = "bossLog";
	/**
	 * Kill count and time messages for one kill arrive within a tick or two of each other,
	 * in either order.
	 */
	private static final int PAIRING_TICKS = 2;

	private final Client client;
	private final ConfigManager configManager;
	private final Gson gson;

	private final Map<String, BossRecord> records = new HashMap<>();
	private String lastKillName;
	private int lastKillTick = Integer.MIN_VALUE;
	private BossLogParser.PersonalBest pendingBest;
	private int pendingBestTick;

	/**
	 * Called with (boss name, time) when a new personal best is set.
	 */
	@Setter
	private BiConsumer<String, String> newPersonalBestListener;

	@Inject
	BossLogTracker(Client client, ConfigManager configManager, Gson gson)
	{
		this.client = client;
		this.configManager = configManager;
		this.gson = gson;
	}

	public void load()
	{
		records.clear();
		String json = configManager.getRSProfileConfiguration(StreamerPopoutPanelsConfig.GROUP, CONFIG_KEY);
		if (json != null)
		{
			try
			{
				BossRecord[] stored = gson.fromJson(json, BossRecord[].class);
				if (stored != null)
				{
					for (BossRecord record : stored)
					{
						if (record != null && record.getName() != null)
						{
							records.put(record.getName(), record);
						}
					}
				}
			}
			catch (JsonParseException e)
			{
				log.debug("Unable to load stored kill counts", e);
			}
		}
		publish();
	}

	public void clear()
	{
		records.clear();
		if (configManager.getRSProfileKey() != null)
		{
			configManager.unsetRSProfileConfiguration(StreamerPopoutPanelsConfig.GROUP, CONFIG_KEY);
		}
		publish();
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		load();
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
		{
			return;
		}

		String message = Text.removeTags(event.getMessage());
		int tick = client.getTickCount();

		BossLogParser.KillCount kc = BossLogParser.parseKillCount(message);
		if (kc != null)
		{
			BossRecord record = records.computeIfAbsent(kc.getName(), name -> new BossRecord(name, 0, null, 0, 0));
			record.setKillCount(kc.getCount());
			record.setLastKill(System.currentTimeMillis());
			lastKillName = kc.getName();
			lastKillTick = tick;

			if (pendingBest != null && tick - pendingBestTick <= PAIRING_TICKS)
			{
				applyPersonalBest(record, pendingBest);
			}
			pendingBest = null;
			changed();
			return;
		}

		BossLogParser.PersonalBest best = BossLogParser.parsePersonalBest(message);
		if (best != null)
		{
			BossRecord record = lastKillName != null && tick - lastKillTick <= PAIRING_TICKS ? records.get(lastKillName) : null;
			if (record != null)
			{
				applyPersonalBest(record, best);
				changed();
			}
			else
			{
				// The kill count message hasn't arrived yet
				pendingBest = best;
				pendingBestTick = tick;
			}
		}
	}

	private void applyPersonalBest(BossRecord record, BossLogParser.PersonalBest best)
	{
		record.setPersonalBest(best.getTime());
		if (best.isNew())
		{
			record.setPersonalBestSet(System.currentTimeMillis());
			BiConsumer<String, String> listener = newPersonalBestListener;
			if (listener != null)
			{
				listener.accept(record.getName(), best.getTime());
			}
		}
	}

	private void changed()
	{
		if (configManager.getRSProfileKey() != null)
		{
			configManager.setRSProfileConfiguration(StreamerPopoutPanelsConfig.GROUP, CONFIG_KEY, gson.toJson(records.values()));
		}
		publish();
	}

	private void publish()
	{
		List<BossRecord> snapshot = new ArrayList<>();
		for (BossRecord record : records.values())
		{
			snapshot.add(record.copy());
		}
		snapshot.sort(Comparator.comparingLong(BossRecord::getLastKill).reversed());
		publish(snapshot);
	}
}
