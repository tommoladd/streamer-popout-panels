package com.streamerpopoutpanels.tracker;

import com.streamerpopoutpanels.ui.SkillsView;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.Subscribe;

/**
 * Tracks XP gained since the plugin started (or the session was reset). Session time only
 * counts time spent logged in. All methods run on the client thread.
 */
public class XpSessionTracker extends SnapshotPublisher<XpSession>
{
	/**
	 * Ticks after login during which stat changes only set the baseline, so the XP loaded at
	 * login doesn't count as gained.
	 */
	private static final int GRACE_TICKS = 2;
	private static final double SECONDS_PER_TICK = 0.6;

	private final Client client;
	private final Map<Skill, Integer> lastXp = new EnumMap<>(Skill.class);
	private final Map<Skill, Long> gained = new EnumMap<>(Skill.class);
	private boolean ready;
	private int ticksSinceLogin;
	private long sessionTicks;

	@Inject
	XpSessionTracker(Client client)
	{
		this.client = client;
	}

	/**
	 * Call on the client thread when the plugin starts while already logged in.
	 */
	public void startLoggedIn()
	{
		setBaseline();
		ready = true;
		publish();
	}

	public void reset()
	{
		gained.clear();
		sessionTicks = 0;
		publish();
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		Integer previous = lastXp.put(event.getSkill(), event.getXp());
		if (ready && previous != null && event.getXp() > previous)
		{
			gained.merge(event.getSkill(), (long) (event.getXp() - previous), Long::sum);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING)
		{
			lastXp.clear();
			ready = false;
			ticksSinceLogin = 0;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!ready && ++ticksSinceLogin >= GRACE_TICKS)
		{
			setBaseline();
			ready = true;
		}
		if (ready)
		{
			sessionTicks++;
		}
		publish();
	}

	private void setBaseline()
	{
		for (Skill skill : SkillsView.LAYOUT)
		{
			lastXp.putIfAbsent(skill, client.getSkillExperience(skill));
		}
	}

	private void publish()
	{
		double hours = sessionTicks * SECONDS_PER_TICK / 3600;
		List<XpSession.SkillGain> skills = new ArrayList<>();
		long total = 0;

		for (Map.Entry<Skill, Long> entry : gained.entrySet())
		{
			Skill skill = entry.getKey();
			long skillGained = entry.getValue();
			total += skillGained;

			int xp = lastXp.getOrDefault(skill, client.getSkillExperience(skill));
			int level = Experience.getLevelForXp(xp);
			long perHour = hours > 0 ? (long) (skillGained / hours) : 0;

			double progress = 1;
			long xpToLevel = 0;
			long secondsToLevel = -1;
			if (level < Experience.MAX_VIRT_LEVEL)
			{
				int levelStart = Experience.getXpForLevel(level);
				int levelEnd = Experience.getXpForLevel(level + 1);
				progress = (xp - levelStart) / (double) (levelEnd - levelStart);
				xpToLevel = levelEnd - xp;
				if (perHour > 0)
				{
					secondsToLevel = xpToLevel * 3600 / perHour;
				}
			}

			skills.add(new XpSession.SkillGain(skill, skillGained, perHour, level, progress, xpToLevel, secondsToLevel));
		}

		skills.sort(Comparator.comparingLong(XpSession.SkillGain::getGained).reversed());
		long totalPerHour = hours > 0 ? (long) (total / hours) : 0;
		publish(new XpSession((long) (sessionTicks * SECONDS_PER_TICK), total, totalPerHour, skills));
	}
}
