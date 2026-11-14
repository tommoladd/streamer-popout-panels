package com.streamerpopoutpanels.tracker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Value;

/**
 * Parses kill count and personal best chat messages (with tags already removed).
 */
final class BossLogParser
{
	private static final Pattern KILL_COUNT = Pattern.compile(
		"^Your (?:completed |subdued )?(.+?) (?:kill |chest |completion |success )?count is: ([\\d,]+)\\.?$");
	private static final Pattern CLUE_COUNT = Pattern.compile("^You have completed ([\\d,]+) (\\w+) Treasure Trails?\\.$");
	private static final String TIME = "(\\d+:\\d{2}(?::\\d{2})?(?:\\.\\d{2})?)";
	private static final Pattern NEW_PERSONAL_BEST = Pattern.compile(TIME + " \\(new personal best\\)");
	private static final Pattern PERSONAL_BEST = Pattern.compile("Personal best: " + TIME);

	@Value
	static class KillCount
	{
		String name;
		int count;
	}

	@Value
	static class PersonalBest
	{
		String time;
		boolean isNew;
	}

	private BossLogParser()
	{
	}

	static KillCount parseKillCount(String message)
	{
		Matcher m = KILL_COUNT.matcher(message);
		if (m.matches())
		{
			String name = m.group(1);
			// Agility course laps use the same message format
			if (name.endsWith(" lap"))
			{
				return null;
			}
			return new KillCount(name, parseCount(m.group(2)));
		}

		m = CLUE_COUNT.matcher(message);
		if (m.matches())
		{
			String tier = m.group(2).toLowerCase();
			return new KillCount("Clue scrolls (" + tier + ")", parseCount(m.group(1)));
		}
		return null;
	}

	static PersonalBest parsePersonalBest(String message)
	{
		Matcher m = NEW_PERSONAL_BEST.matcher(message);
		if (m.find())
		{
			return new PersonalBest(m.group(1), true);
		}
		m = PERSONAL_BEST.matcher(message);
		if (m.find())
		{
			return new PersonalBest(m.group(1), false);
		}
		return null;
	}

	private static int parseCount(String count)
	{
		try
		{
			return Integer.parseInt(count.replace(",", ""));
		}
		catch (NumberFormatException e)
		{
			return 0;
		}
	}
}
