package com.streamerpopoutpanels;

import com.streamerpopoutpanels.ui.SkillsView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.runelite.api.Experience;
import net.runelite.api.Skill;

/**
 * Parses the goals config. One goal per line (or comma separated):
 * <ul>
 *     <li>{@code Slayer 99} - reach a level (up to 126 for virtual levels)</li>
 *     <li>{@code Fishing 50m} or {@code Fishing 13034431 xp} - reach an amount of XP</li>
 *     <li>{@code Total 2000} - reach a total level</li>
 * </ul>
 */
final class GoalParser
{
	private static final Pattern LINE = Pattern.compile("^(.+?)\\s+([\\d.,]+)\\s*([km])?\\s*(xp)?$", Pattern.CASE_INSENSITIVE);
	private static final int MAX_XP = 200_000_000;
	private static final Map<String, Skill> ALIASES = new HashMap<>();

	static
	{
		for (Skill skill : SkillsView.LAYOUT)
		{
			ALIASES.put(skill.getName().toLowerCase(Locale.ENGLISH), skill);
		}
		ALIASES.put("att", Skill.ATTACK);
		ALIASES.put("str", Skill.STRENGTH);
		ALIASES.put("def", Skill.DEFENCE);
		ALIASES.put("defense", Skill.DEFENCE);
		ALIASES.put("hp", Skill.HITPOINTS);
		ALIASES.put("range", Skill.RANGED);
		ALIASES.put("ranging", Skill.RANGED);
		ALIASES.put("pray", Skill.PRAYER);
		ALIASES.put("mage", Skill.MAGIC);
		ALIASES.put("wc", Skill.WOODCUTTING);
		ALIASES.put("fm", Skill.FIREMAKING);
		ALIASES.put("herb", Skill.HERBLORE);
		ALIASES.put("agil", Skill.AGILITY);
		ALIASES.put("thiev", Skill.THIEVING);
		ALIASES.put("slay", Skill.SLAYER);
		ALIASES.put("rc", Skill.RUNECRAFT);
		ALIASES.put("runecrafting", Skill.RUNECRAFT);
		ALIASES.put("con", Skill.CONSTRUCTION);
		ALIASES.put("hunt", Skill.HUNTER);
		ALIASES.put("sail", Skill.SAILING);
	}

	private GoalParser()
	{
	}

	static List<Goal> parse(String text)
	{
		List<Goal> goals = new ArrayList<>();
		if (text == null)
		{
			return goals;
		}

		// Commas only separate goals when followed by a name, so "13,034,431 xp" stays whole
		for (String line : text.split("[\\n;]|,(?=\\s*[A-Za-z])"))
		{
			Goal goal = parseLine(line.trim());
			if (goal != null)
			{
				goals.add(goal);
			}
		}
		return goals;
	}

	private static Goal parseLine(String line)
	{
		Matcher m = LINE.matcher(line);
		if (!m.matches())
		{
			return null;
		}

		String name = m.group(1).trim().toLowerCase(Locale.ENGLISH);
		double amount;
		try
		{
			amount = Double.parseDouble(m.group(2).replace(",", ""));
		}
		catch (NumberFormatException e)
		{
			return null;
		}
		String suffix = m.group(3) == null ? "" : m.group(3).toLowerCase(Locale.ENGLISH);
		boolean xpSuffix = m.group(4) != null;

		if (name.equals("total") || name.equals("total level") || name.equals("overall"))
		{
			int level = (int) amount;
			return level > 0 ? new Goal("Total level " + level, null, level) : null;
		}

		Skill skill = ALIASES.get(name);
		if (skill == null)
		{
			return null;
		}

		if (suffix.equals("k"))
		{
			amount *= 1_000;
		}
		else if (suffix.equals("m"))
		{
			amount *= 1_000_000;
		}

		if (suffix.isEmpty() && !xpSuffix && amount <= Experience.MAX_VIRT_LEVEL)
		{
			int level = (int) amount;
			if (level < 2)
			{
				return null;
			}
			return new Goal(skill.getName() + " level " + level, skill, Experience.getXpForLevel(level));
		}

		long xp = Math.min(MAX_XP, (long) amount);
		if (xp <= 0)
		{
			return null;
		}
		return new Goal(skill.getName() + " " + formatXp(xp) + " xp", skill, xp);
	}

	private static String formatXp(long xp)
	{
		if (xp >= 1_000_000 && xp % 100_000 == 0)
		{
			return stripZero(xp / 1_000_000d) + "M";
		}
		if (xp >= 1_000 && xp % 100 == 0)
		{
			return stripZero(xp / 1_000d) + "K";
		}
		return String.format(Locale.ENGLISH, "%,d", xp);
	}

	private static String stripZero(double value)
	{
		return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
	}
}
