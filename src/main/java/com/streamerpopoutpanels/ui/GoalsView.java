package com.streamerpopoutpanels.ui;

import com.streamerpopoutpanels.Goal;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.runelite.api.Skill;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.FontManager;

public class GoalsView extends ListView
{
	private static final int ROW_H = 46;
	private static final int ICON = 24;
	private static final Color BAR_COLOR = new Color(0x81C784);

	private final SkillIconManager skillIconManager;
	private List<Goal> goals = new ArrayList<>();
	private Map<Skill, Integer> experience = new EnumMap<>(Skill.class);
	private int totalLevel;

	public GoalsView(SkillIconManager skillIconManager)
	{
		this.skillIconManager = skillIconManager;
	}

	public void setGoals(List<Goal> goals)
	{
		this.goals = goals;
		repaint();
	}

	public void setProgress(Map<Skill, Integer> experience, int totalLevel)
	{
		this.experience = experience;
		this.totalLevel = totalLevel;
		repaint();
	}

	@Override
	protected void paintContent(Graphics2D g)
	{
		if (goals.isEmpty())
		{
			drawEmpty(g, "Add goals in the plugin settings");
			return;
		}

		int x = PADDING;
		int y = PADDING;
		int right = x + ROW_W - 8;
		for (Goal goal : goals)
		{
			if (y + ROW_H > naturalHeight())
			{
				break;
			}
			drawSlot(g, x, y, ROW_W, ROW_H);

			long current = goal.isTotalLevel() ? totalLevel : experience.getOrDefault(goal.getSkill(), 0);
			long target = goal.getTarget();
			double progress = target > 0 ? Math.min(1, current / (double) target) : 1;
			boolean done = current >= target;

			int textX = x + 8;
			if (!goal.isTotalLevel())
			{
				BufferedImage icon = skillIconManager.getSkillImage(goal.getSkill(), false);
				if (icon != null)
				{
					drawIcon(g, icon, x + 8, y + (ROW_H - ICON) / 2, ICON);
				}
				textX += ICON + 8;
			}

			g.setFont(FontManager.getRunescapeFont());
			String percent = String.format(Locale.ENGLISH, "%.1f%%", progress * 100);
			int percentW = g.getFontMetrics().stringWidth(percent);
			Draw.shadowed(g, Draw.truncate(goal.getLabel(), g.getFontMetrics(), right - textX - percentW - 6), textX, y + 15, Color.WHITE);
			Draw.shadowedRight(g, percent, right, y + 15, done ? Draw.GREEN : Draw.YELLOW);

			Draw.bar(g, textX, y + 20, right - textX, 8, progress, done ? Draw.GREEN : BAR_COLOR);

			g.setFont(FontManager.getRunescapeSmallFont());
			String detail;
			if (done)
			{
				detail = "Complete!";
			}
			else if (goal.isTotalLevel())
			{
				detail = Draw.number(target - current) + " levels to go";
			}
			else
			{
				detail = Draw.number(target - current) + " xp to go";
			}
			Draw.shadowed(g, detail, textX, y + 40, done ? Draw.GREEN : Draw.MUTED);

			y += ROW_H + ROW_GAP;
		}
	}
}
