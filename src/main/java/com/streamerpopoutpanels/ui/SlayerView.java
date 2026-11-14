package com.streamerpopoutpanels.ui;

import com.streamerpopoutpanels.tracker.SlayerTask;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import net.runelite.api.Skill;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.FontManager;

public class SlayerView extends PopoutView
{
	private static final int WIDTH = 280;
	private static final int HEIGHT = 108;
	private static final int PADDING = 6;
	private static final int ICON = 32;
	private static final Color BAR_COLOR = new Color(0xE57373);

	private final SkillIconManager skillIconManager;
	private SlayerTask task;

	public SlayerView(SkillIconManager skillIconManager)
	{
		this.skillIconManager = skillIconManager;
	}

	public void setTask(SlayerTask task)
	{
		this.task = task;
		repaint();
	}

	@Override
	protected int naturalWidth()
	{
		return WIDTH;
	}

	@Override
	protected int naturalHeight()
	{
		return HEIGHT;
	}

	@Override
	protected void paintContent(Graphics2D g)
	{
		int x = PADDING;
		int y = PADDING;
		int w = WIDTH - PADDING * 2;
		int h = HEIGHT - PADDING * 2;
		int right = x + w - 8;
		drawSlot(g, x, y, w, h);

		BufferedImage icon = skillIconManager.getSkillImage(Skill.SLAYER, false);
		if (icon != null)
		{
			drawIcon(g, icon, x + 8, y + 8, ICON);
		}

		int textX = x + 8 + ICON + 10;
		g.setFont(FontManager.getRunescapeSmallFont());
		Draw.shadowed(g, "Slayer task", textX, y + 16, Draw.MUTED);

		if (task == null || task.getName() == null)
		{
			g.setFont(FontManager.getRunescapeBoldFont());
			Draw.shadowed(g, task == null ? "Log in to see your task" : "No slayer task", textX, y + 34, Color.WHITE);
		}
		else
		{
			g.setFont(FontManager.getRunescapeBoldFont());
			Draw.shadowed(g, Draw.truncate(task.getName(), g.getFontMetrics(), right - textX), textX, y + 34, Color.WHITE);

			g.setFont(FontManager.getRunescapeSmallFont());
			if (task.getLocation() != null)
			{
				Draw.shadowed(g, Draw.truncate(task.getLocation(), g.getFontMetrics(), right - textX), textX, y + 48, Draw.MUTED);
			}

			int barY = y + 54;
			double done = task.getOriginal() > 0 ? 1 - task.getRemaining() / (double) task.getOriginal() : 0;
			Draw.bar(g, x + 8, barY, w - 16, 8, done, BAR_COLOR);
			Draw.shadowed(g, Draw.number(task.getRemaining()) + " left", x + 8, barY + 22, Draw.YELLOW);
			if (task.getOriginal() > 0)
			{
				Draw.shadowedRight(g, "of " + Draw.number(task.getOriginal()), right, barY + 22, Draw.MUTED);
			}
		}

		if (task != null)
		{
			g.setFont(FontManager.getRunescapeSmallFont());
			String footer = "Points: " + Draw.number(task.getPoints()) + "    Streak: " + Draw.number(task.getStreak());
			Draw.shadowedRight(g, footer, right, y + 16, Draw.MUTED);
		}
	}
}
