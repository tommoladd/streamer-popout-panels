package com.streamerpopoutpanels;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns game chat messages (with tags already removed) into achievements.
 */
final class AchievementParser
{
	private static final Pattern COLLECTION_LOG = Pattern.compile("^New item added to your collection log: (.+)$");
	private static final Pattern QUEST = Pattern.compile("^Congratulations, you've completed a quest: (.+?)!?$");
	private static final Pattern COMBAT_TASK = Pattern.compile(
		"^Congratulations, you've completed an? (\\w+) combat task: (.+?)(?: \\(\\d+ points?\\))?\\.?$");
	private static final Pattern DIARY = Pattern.compile(
		"^Congratulations! You have completed all of the (\\w+) tasks in the (.+?) area\\..*$");
	private static final Pattern VALUABLE_DROP = Pattern.compile("^Valuable drop: (.+)$");
	private static final Pattern UNTRADEABLE_DROP = Pattern.compile("^Untradeable drop: (.+)$");

	private AchievementParser()
	{
	}

	/**
	 * @param message chat message with tags removed
	 * @param timestamp time to stamp the achievement with
	 * @return the achievement, or null if the message isn't one
	 */
	static Achievement parse(String message, long timestamp)
	{
		Matcher m;

		if ((m = COLLECTION_LOG.matcher(message)).matches())
		{
			return new Achievement(AchievementType.COLLECTION_LOG, m.group(1), null, timestamp);
		}
		if ((m = QUEST.matcher(message)).matches())
		{
			return new Achievement(AchievementType.QUEST, m.group(1), null, timestamp);
		}
		if ((m = COMBAT_TASK.matcher(message)).matches())
		{
			return new Achievement(AchievementType.COMBAT_TASK, capitalize(m.group(1)) + ": " + m.group(2), null, timestamp);
		}
		if ((m = DIARY.matcher(message)).matches())
		{
			return new Achievement(AchievementType.DIARY, m.group(2) + " " + capitalize(m.group(1)), null, timestamp);
		}
		if ((m = VALUABLE_DROP.matcher(message)).matches())
		{
			return new Achievement(AchievementType.DROP, m.group(1), null, timestamp);
		}
		if ((m = UNTRADEABLE_DROP.matcher(message)).matches())
		{
			return new Achievement(AchievementType.DROP, m.group(1), null, timestamp);
		}
		if (message.startsWith("You have a funny feeling like you're being followed")
			|| message.startsWith("You feel something weird sneaking into your backpack"))
		{
			return new Achievement(AchievementType.PET, "New pet!", null, timestamp);
		}
		if (message.startsWith("You have a funny feeling like you would have been followed"))
		{
			return new Achievement(AchievementType.PET, "Pet drop (duplicate)", null, timestamp);
		}
		return null;
	}

	private static String capitalize(String s)
	{
		return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
	}
}
