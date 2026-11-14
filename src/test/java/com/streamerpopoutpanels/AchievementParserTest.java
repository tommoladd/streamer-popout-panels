package com.streamerpopoutpanels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import net.runelite.client.util.Text;
import org.junit.Test;

public class AchievementParserTest
{
	private static Achievement parse(String message)
	{
		return AchievementParser.parse(Text.removeTags(message), 0);
	}

	private static void assertAchievement(String message, AchievementType type, String text)
	{
		Achievement a = parse(message);
		assertEquals(type, a.getType());
		assertEquals(text, a.getText());
	}

	@Test
	public void collectionLog()
	{
		assertAchievement("New item added to your collection log: <col=ef1020>Abyssal whip</col>",
			AchievementType.COLLECTION_LOG, "Abyssal whip");
	}

	@Test
	public void quest()
	{
		assertAchievement("Congratulations, you've completed a quest: <col=0a0ca5>Cook's Assistant</col>",
			AchievementType.QUEST, "Cook's Assistant");
	}

	@Test
	public void combatTask()
	{
		assertAchievement("Congratulations, you've completed an easy combat task: <col=06600c>Noxious Foe</col> (1 point).",
			AchievementType.COMBAT_TASK, "Easy: Noxious Foe");
		assertAchievement("Congratulations, you've completed a grandmaster combat task: <col=06600c>Perfect Olm (Solo)</col> (6 points).",
			AchievementType.COMBAT_TASK, "Grandmaster: Perfect Olm (Solo)");
	}

	@Test
	public void diary()
	{
		assertAchievement("Congratulations! You have completed all of the hard tasks in the Ardougne area. Speak to Two-pints to claim your reward.",
			AchievementType.DIARY, "Ardougne Hard");
	}

	@Test
	public void drops()
	{
		assertAchievement("<col=ef1020>Valuable drop: 1 x Dragon pickaxe (1,234,567 coins)</col>",
			AchievementType.DROP, "1 x Dragon pickaxe (1,234,567 coins)");
		assertAchievement("<col=ef1020>Untradeable drop: Pet snakeling</col>",
			AchievementType.DROP, "Pet snakeling");
	}

	@Test
	public void pets()
	{
		assertAchievement("You have a funny feeling like you're being followed.", AchievementType.PET, "New pet!");
		assertAchievement("You feel something weird sneaking into your backpack.", AchievementType.PET, "New pet!");
		assertAchievement("You have a funny feeling like you would have been followed...", AchievementType.PET, "Pet drop (duplicate)");
	}

	@Test
	public void ignoresOtherMessages()
	{
		assertNull(parse("Welcome to Old School RuneScape."));
		assertNull(parse("Your Zulrah kill count is: <col=ff0000>12</col>."));
	}
}
