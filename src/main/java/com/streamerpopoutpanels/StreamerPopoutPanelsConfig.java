package com.streamerpopoutpanels;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(StreamerPopoutPanelsConfig.GROUP)
public interface StreamerPopoutPanelsConfig extends Config
{
	String GROUP = "streamer-popout-panels";

	@ConfigSection(
		name = "Panels",
		description = "Choose which popout windows are open",
		position = 0
	)
	String panelsSection = "panels";

	@ConfigSection(
		name = "Appearance",
		description = "How the popout windows look",
		position = 1
	)
	String appearanceSection = "appearance";

	@ConfigSection(
		name = "Achievements",
		description = "What appears in the recent achievements panel",
		position = 2
	)
	String achievementsSection = "achievements";

	@ConfigSection(
		name = "Goals",
		description = "Goals shown in the goals panel",
		position = 3
	)
	String goalsSection = "goals";

	@ConfigItem(
		keyName = "showInventory",
		name = "Inventory",
		description = "Open the inventory popout window",
		section = panelsSection,
		position = 0
	)
	default boolean showInventory()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showEquipment",
		name = "Equipment",
		description = "Open the worn equipment popout window",
		section = panelsSection,
		position = 1
	)
	default boolean showEquipment()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showSkills",
		name = "Skills",
		description = "Open the skill levels popout window",
		section = panelsSection,
		position = 2
	)
	default boolean showSkills()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showAchievements",
		name = "Recent achievements",
		description = "Open the recent achievements popout window",
		section = panelsSection,
		position = 3
	)
	default boolean showAchievements()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showXpTracker",
		name = "Session XP",
		description = "Open the session XP tracker popout window",
		section = panelsSection,
		position = 4
	)
	default boolean showXpTracker()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showLoot",
		name = "Loot tracker",
		description = "Open the session loot popout window. Loot from chests, clues and raids needs the Loot Tracker plugin to be enabled",
		section = panelsSection,
		position = 5
	)
	default boolean showLoot()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showSlayer",
		name = "Slayer task",
		description = "Open the slayer task popout window",
		section = panelsSection,
		position = 6
	)
	default boolean showSlayer()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showGoals",
		name = "Goals",
		description = "Open the goal progress popout window. Set your goals in the Goals section",
		section = panelsSection,
		position = 7
	)
	default boolean showGoals()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showBossLog",
		name = "Kill counts",
		description = "Open the kill counts and personal bests popout window",
		section = panelsSection,
		position = 8
	)
	default boolean showBossLog()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showVitals",
		name = "Vitals",
		description = "Open the hitpoints, prayer and run energy popout window",
		section = panelsSection,
		position = 9
	)
	default boolean showVitals()
	{
		return false;
	}

	@ConfigItem(
		keyName = "backgroundColor",
		name = "Background colour",
		description = "Background colour of the popout windows. Use a solid colour such as green if you want to chroma key it out in OBS",
		section = appearanceSection,
		position = 0
	)
	default Color backgroundColor()
	{
		return new Color(30, 30, 30);
	}

	@ConfigItem(
		keyName = "alwaysOnTop",
		name = "Always on top",
		description = "Keep the popout windows above other windows",
		section = appearanceSection,
		position = 1
	)
	default boolean alwaysOnTop()
	{
		return false;
	}

	@Range(min = 1, max = 50)
	@ConfigItem(
		keyName = "maxAchievements",
		name = "Achievements shown",
		description = "How many recent achievements to show",
		section = achievementsSection,
		position = 0
	)
	default int maxAchievements()
	{
		return 8;
	}

	@Range(max = 300)
	@Units(Units.SECONDS)
	@ConfigItem(
		keyName = "highlightSeconds",
		name = "Highlight new for",
		description = "How long new achievements and personal bests are highlighted for. 0 disables the highlight",
		section = achievementsSection,
		position = 1
	)
	default int highlightSeconds()
	{
		return 15;
	}

	@ConfigItem(
		keyName = "trackLevelUps",
		name = "Level ups",
		description = "Show skill level ups",
		section = achievementsSection,
		position = 2
	)
	default boolean trackLevelUps()
	{
		return true;
	}

	@ConfigItem(
		keyName = "trackCollectionLog",
		name = "Collection log",
		description = "Show new collection log slots. Requires the in-game collection log chat notification to be enabled",
		section = achievementsSection,
		position = 3
	)
	default boolean trackCollectionLog()
	{
		return true;
	}

	@ConfigItem(
		keyName = "trackQuests",
		name = "Quests",
		description = "Show completed quests",
		section = achievementsSection,
		position = 4
	)
	default boolean trackQuests()
	{
		return true;
	}

	@ConfigItem(
		keyName = "trackCombatAchievements",
		name = "Combat tasks",
		description = "Show completed combat achievement tasks",
		section = achievementsSection,
		position = 5
	)
	default boolean trackCombatAchievements()
	{
		return true;
	}

	@ConfigItem(
		keyName = "trackDiaries",
		name = "Achievement diaries",
		description = "Show completed achievement diary tiers",
		section = achievementsSection,
		position = 6
	)
	default boolean trackDiaries()
	{
		return true;
	}

	@ConfigItem(
		keyName = "trackPets",
		name = "Pets",
		description = "Show pet drops",
		section = achievementsSection,
		position = 7
	)
	default boolean trackPets()
	{
		return true;
	}

	@ConfigItem(
		keyName = "trackDrops",
		name = "Valuable drops",
		description = "Show valuable and untradeable drops. Requires the in-game loot drop notifications to be enabled",
		section = achievementsSection,
		position = 8
	)
	default boolean trackDrops()
	{
		return true;
	}

	@ConfigItem(
		keyName = "trackPersonalBests",
		name = "Personal bests",
		description = "Show new boss personal best times",
		section = achievementsSection,
		position = 9
	)
	default boolean trackPersonalBests()
	{
		return true;
	}

	@ConfigItem(
		keyName = "goals",
		name = "Goals",
		description = "One goal per line. Examples: 'Slayer 99' for a level, 'Fishing 50m' for an amount of XP, 'Total 2000' for total level",
		section = goalsSection,
		position = 0
	)
	default String goals()
	{
		return "Slayer 99\nTotal 2000";
	}
}
