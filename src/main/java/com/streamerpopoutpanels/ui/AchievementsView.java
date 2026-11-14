package com.streamerpopoutpanels.ui;

import com.streamerpopoutpanels.Achievement;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Skill;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.FontManager;

/**
 * Recent achievements, newest first.
 */
public class AchievementsView extends ListView
{
	private static final int ROW_H = 40;
	private static final int ICON = 24;

	private final SkillIconManager skillIconManager;
	private List<Achievement> entries = new ArrayList<>();

	public AchievementsView(SkillIconManager skillIconManager)
	{
		this.skillIconManager = skillIconManager;
	}

	/**
	 * @param entries achievements, newest first
	 */
	public void setEntries(List<Achievement> entries)
	{
		this.entries = entries;
		contentChanged();
	}

	@Override
	protected boolean anyHighlighted()
	{
		return entries.stream().anyMatch(a -> highlightStrength(a.getTimestamp()) > 0);
	}

	@Override
	protected void paintContent(Graphics2D g)
	{
		if (entries.isEmpty())
		{
			drawEmpty(g, "No achievements yet");
			return;
		}

		int y = PADDING;
		for (Achievement a : entries)
		{
			if (y + ROW_H > naturalHeight())
			{
				break;
			}

			int x = PADDING;
			drawSlot(g, x, y, ROW_W, ROW_H);
			drawHighlight(g, x, y, ROW_W, ROW_H, highlightStrength(a.getTimestamp()));

			Color typeColor = a.getType().getColor();
			g.setColor(typeColor);
			g.fillRect(x, y + 4, 3, ROW_H - 8);

			int iconX = x + 8;
			BufferedImage icon = skillIcon(a.getSkill());
			if (icon != null)
			{
				drawIcon(g, icon, iconX, y + (ROW_H - ICON) / 2, ICON);
			}
			else
			{
				g.setColor(Draw.withAlpha(typeColor, 200));
				g.fillOval(iconX + 4, y + (ROW_H - 16) / 2, 16, 16);
			}

			int textX = iconX + ICON + 8;
			int textW = x + ROW_W - textX - 6;

			g.setFont(FontManager.getRunescapeSmallFont());
			Draw.shadowed(g, a.getType().getLabel(), textX, y + 14, typeColor);

			g.setFont(FontManager.getRunescapeFont());
			Draw.shadowed(g, Draw.truncate(a.getText(), g.getFontMetrics(), textW), textX, y + 32, Color.WHITE);

			y += ROW_H + ROW_GAP;
		}
	}

	private BufferedImage skillIcon(String skillName)
	{
		if (skillName == null)
		{
			return null;
		}
		try
		{
			return skillIconManager.getSkillImage(Skill.valueOf(skillName), false);
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}
}
