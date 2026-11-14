package com.streamerpopoutpanels.ui;

import com.streamerpopoutpanels.tracker.XpSession;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.FontManager;

public class XpTrackerView extends ListView
{
	private static final int HEADER_H = 36;
	private static final int ROW_H = 44;
	private static final int ICON = 24;
	private static final Color BAR_COLOR = new Color(0x4FC3F7);

	private final SkillIconManager skillIconManager;
	private XpSession session = new XpSession(0, 0, 0, Collections.emptyList());

	public XpTrackerView(SkillIconManager skillIconManager)
	{
		this.skillIconManager = skillIconManager;
	}

	public void setSession(XpSession session)
	{
		this.session = session;
		repaint();
	}

	@Override
	protected void paintContent(Graphics2D g)
	{
		int x = PADDING;
		int y = PADDING;
		int right = x + ROW_W - 8;

		// Header: session time and totals
		drawSlot(g, x, y, ROW_W, HEADER_H);
		g.setFont(FontManager.getRunescapeSmallFont());
		Draw.shadowed(g, "Session time", x + 8, y + 14, Draw.MUTED);
		Draw.shadowedRight(g, Draw.shortNumber(session.getTotalPerHour()) + " xp/hr", right, y + 14, Draw.MUTED);
		g.setFont(FontManager.getRunescapeBoldFont());
		Draw.shadowed(g, Draw.duration(session.getElapsedSeconds()), x + 8, y + 30, Color.WHITE);
		Draw.shadowedRight(g, "+" + Draw.number(session.getTotalGained()) + " xp", right, y + 30, Draw.YELLOW);
		y += HEADER_H + ROW_GAP;

		if (session.getSkills().isEmpty())
		{
			g.setFont(FontManager.getRunescapeFont());
			g.setColor(Draw.MUTED);
			String text = "Gain some XP to start tracking";
			g.drawString(text, (WIDTH - g.getFontMetrics().stringWidth(text)) / 2, y + 20);
			return;
		}

		for (XpSession.SkillGain skill : session.getSkills())
		{
			if (y + ROW_H > naturalHeight())
			{
				break;
			}

			drawSlot(g, x, y, ROW_W, ROW_H);
			BufferedImage icon = skillIconManager.getSkillImage(skill.getSkill(), false);
			if (icon != null)
			{
				drawIcon(g, icon, x + 8, y + (ROW_H - ICON) / 2, ICON);
			}

			int textX = x + 8 + ICON + 8;
			g.setFont(FontManager.getRunescapeFont());
			Draw.shadowed(g, skill.getSkill().getName(), textX, y + 15, Color.WHITE);
			Draw.shadowedRight(g, "+" + Draw.number(skill.getGained()), right, y + 15, Draw.YELLOW);

			g.setFont(FontManager.getRunescapeSmallFont());
			Draw.shadowed(g, "Lvl " + skill.getLevel() + "  " + Draw.shortNumber(skill.getPerHour()) + "/hr", textX, y + 28, Draw.MUTED);
			String ttl = skill.getSecondsToLevel() >= 0 ? Draw.duration(skill.getSecondsToLevel()) + " to level" : "";
			Draw.shadowedRight(g, ttl, right, y + 28, Draw.MUTED);

			Draw.bar(g, textX, y + 33, right - textX, 5, skill.getLevelProgress(), BAR_COLOR);

			y += ROW_H + ROW_GAP;
		}
	}
}
