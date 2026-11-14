package com.streamerpopoutpanels;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PanelType
{
	INVENTORY("Inventory", "showInventory", 250, 340),
	EQUIPMENT("Equipment", "showEquipment", 230, 330),
	SKILLS("Skills", "showSkills", 300, 440),
	ACHIEVEMENTS("Recent Achievements", "showAchievements", 360, 420),
	XP_TRACKER("Session XP", "showXpTracker", 360, 420),
	LOOT("Loot Tracker", "showLoot", 360, 440),
	SLAYER("Slayer Task", "showSlayer", 340, 170),
	GOALS("Goals", "showGoals", 360, 260),
	BOSS_LOG("Kill Counts", "showBossLog", 340, 400),
	VITALS("Vitals", "showVitals", 300, 160);

	private final String title;
	/**
	 * Config key of the toggle that opens this panel.
	 */
	private final String configKey;
	private final int defaultWidth;
	private final int defaultHeight;

	/**
	 * Config key the window position and size are stored under.
	 */
	public String getBoundsKey()
	{
		return "bounds." + name().toLowerCase();
	}

	/**
	 * Title of the popout window. Kept stable so OBS window capture can find it again.
	 */
	public String getWindowTitle()
	{
		return "Streamer Popout - " + title;
	}
}
