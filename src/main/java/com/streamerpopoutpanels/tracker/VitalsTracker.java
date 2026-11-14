package com.streamerpopoutpanels.tracker;

import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;

/**
 * Reads hitpoints, prayer and run energy once per tick, publishing only when something
 * changed. All methods run on the client thread.
 */
public class VitalsTracker extends SnapshotPublisher<Vitals>
{
	/**
	 * {@link VarPlayerID#POISON} values at or above this are venom rather than poison.
	 */
	private static final int VENOM_THRESHOLD = 1_000_000;

	private final Client client;
	private Vitals last;

	@Inject
	VitalsTracker(Client client)
	{
		this.client = client;
	}

	/**
	 * Publish on the next tick even if nothing changed, e.g. after the view was recreated.
	 */
	public void forceRefresh()
	{
		last = null;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		int poison = client.getVarpValue(VarPlayerID.POISON);
		Vitals.HitpointsStatus status;
		if (poison >= VENOM_THRESHOLD)
		{
			status = Vitals.HitpointsStatus.VENOMED;
		}
		else if (poison > 0)
		{
			status = Vitals.HitpointsStatus.POISONED;
		}
		else if (client.getVarpValue(VarPlayerID.DISEASE) > 0)
		{
			status = Vitals.HitpointsStatus.DISEASED;
		}
		else
		{
			status = Vitals.HitpointsStatus.NORMAL;
		}

		Vitals vitals = new Vitals(
			client.getBoostedSkillLevel(Skill.HITPOINTS),
			client.getRealSkillLevel(Skill.HITPOINTS),
			status,
			client.getBoostedSkillLevel(Skill.PRAYER),
			client.getRealSkillLevel(Skill.PRAYER),
			client.getEnergy() / 100,
			client.getVarbitValue(VarbitID.STAMINA_ACTIVE) == 1);

		if (!vitals.equals(last))
		{
			last = vitals;
			publish(vitals);
		}
	}
}
