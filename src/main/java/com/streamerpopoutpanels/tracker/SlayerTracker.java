package com.streamerpopoutpanels.tracker;

import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.DBTableID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;

/**
 * Reads the current slayer task from game variables. All methods run on the client thread.
 */
@Slf4j
public class SlayerTracker extends SnapshotPublisher<SlayerTask>
{
	/**
	 * Value of {@link VarPlayerID#SLAYER_TARGET} for boss tasks, whose boss is in
	 * {@link VarbitID#SLAYER_TARGET_BOSSID}. From [proc,helper_slayer_current_assignment].
	 */
	private static final int BOSS_TASK = 98;

	private final Client client;
	/**
	 * Varps arrive in bursts (e.g. at login), so refresh at most once per tick.
	 */
	private boolean dirty;

	@Inject
	SlayerTracker(Client client)
	{
		this.client = client;
	}

	public void refreshNextTick()
	{
		dirty = true;
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int varp = event.getVarpId();
		int varbit = event.getVarbitId();
		if (varp == VarPlayerID.SLAYER_COUNT || varp == VarPlayerID.SLAYER_TARGET || varp == VarPlayerID.SLAYER_AREA
			|| varp == VarPlayerID.SLAYER_COUNT_ORIGINAL || varbit == VarbitID.SLAYER_POINTS
			|| varbit == VarbitID.SLAYER_TASKS_COMPLETED || varbit == VarbitID.SLAYER_TARGET_BOSSID)
		{
			dirty = true;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (dirty && client.getGameState() == GameState.LOGGED_IN)
		{
			dirty = false;
			refresh();
		}
	}

	private void refresh()
	{
		int remaining = client.getVarpValue(VarPlayerID.SLAYER_COUNT);
		int points = client.getVarbitValue(VarbitID.SLAYER_POINTS);
		int streak = client.getVarbitValue(VarbitID.SLAYER_TASKS_COMPLETED);

		if (remaining <= 0)
		{
			publish(new SlayerTask(null, null, 0, 0, points, streak));
			return;
		}

		String name = taskName();
		int original = Math.max(remaining, client.getVarpValue(VarPlayerID.SLAYER_COUNT_ORIGINAL));
		publish(new SlayerTask(name != null ? name : "Unknown task", areaName(), remaining, original, points, streak));
	}

	private String taskName()
	{
		try
		{
			int taskId = client.getVarpValue(VarPlayerID.SLAYER_TARGET);
			int taskRow;
			if (taskId == BOSS_TASK)
			{
				List<Integer> rows = client.getDBRowsByValue(DBTableID.SlayerTaskSublist.ID,
					DBTableID.SlayerTaskSublist.COL_TASK_SUBTABLE_ID, 0, client.getVarbitValue(VarbitID.SLAYER_TARGET_BOSSID));
				if (rows.isEmpty())
				{
					return null;
				}
				taskRow = (Integer) client.getDBTableField(rows.get(0), DBTableID.SlayerTaskSublist.COL_TASK, 0)[0];
			}
			else
			{
				List<Integer> rows = client.getDBRowsByValue(DBTableID.SlayerTask.ID, DBTableID.SlayerTask.COL_ID, 0, taskId);
				if (rows.isEmpty())
				{
					return null;
				}
				taskRow = rows.get(0);
			}
			return (String) client.getDBTableField(taskRow, DBTableID.SlayerTask.COL_NAME_UPPERCASE, 0)[0];
		}
		catch (RuntimeException e)
		{
			log.debug("Unable to look up slayer task name", e);
			return null;
		}
	}

	private String areaName()
	{
		int areaId = client.getVarpValue(VarPlayerID.SLAYER_AREA);
		if (areaId <= 0)
		{
			return null;
		}
		try
		{
			List<Integer> rows = client.getDBRowsByValue(DBTableID.SlayerArea.ID, DBTableID.SlayerArea.COL_AREA_ID, 0, areaId);
			return rows.isEmpty() ? null : (String) client.getDBTableField(rows.get(0), DBTableID.SlayerArea.COL_AREA_NAME_IN_HELPER, 0)[0];
		}
		catch (RuntimeException e)
		{
			log.debug("Unable to look up slayer area name", e);
			return null;
		}
	}
}
