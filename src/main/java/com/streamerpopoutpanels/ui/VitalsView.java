package com.streamerpopoutpanels.ui;

import com.streamerpopoutpanels.tracker.Vitals;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import net.runelite.api.Skill;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.FontManager;

public class VitalsView extends PopoutView
{
	private static final int WIDTH = 260;
	private static final int PADDING = 6;
	private static final int ROW_H = 30;
	private static final int ROW_GAP = 4;
	private static final int ROWS = 3;
	private static final int ICON = 20;
	private static final int BAR_H = 18;

	private static final Color HITPOINTS_COLOR = new Color(200, 40, 20);
	private static final Color POISONED_COLOR = new Color(0, 145, 0);
	private static final Color VENOMED_COLOR = new Color(0, 90, 70);
	private static final Color DISEASED_COLOR = new Color(200, 150, 40);
	private static final Color PRAYER_COLOR = new Color(40, 170, 210);
	private static final Color RUN_COLOR = new Color(210, 190, 50);
	private static final Color STAMINA_COLOR = new Color(170, 115, 60);

	private final SkillIconManager skillIconManager;
	private Vitals vitals;

	public VitalsView(SkillIconManager skillIconManager)
	{
		this.skillIconManager = skillIconManager;
	}

	public void setVitals(Vitals vitals)
	{
		this.vitals = vitals;
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
		return ROWS * ROW_H + (ROWS - 1) * ROW_GAP + PADDING * 2;
	}

	@Override
	protected void paintContent(Graphics2D g)
	{
		Vitals v = vitals != null ? vitals : new Vitals(0, 0, Vitals.HitpointsStatus.NORMAL, 0, 0, 0, false);
		int y = PADDING;

		String hpSuffix;
		Color hpColor;
		switch (v.getStatus())
		{
			case POISONED:
				hpSuffix = "  Poisoned";
				hpColor = POISONED_COLOR;
				break;
			case VENOMED:
				hpSuffix = "  Venomed";
				hpColor = VENOMED_COLOR;
				break;
			case DISEASED:
				hpSuffix = "  Diseased";
				hpColor = DISEASED_COLOR;
				break;
			default:
				hpSuffix = "";
				hpColor = HITPOINTS_COLOR;
		}
		drawRow(g, y, Skill.HITPOINTS, v.getHitpoints(), v.getMaxHitpoints(),
			v.getHitpoints() + " / " + v.getMaxHitpoints() + hpSuffix, hpColor);
		y += ROW_H + ROW_GAP;

		drawRow(g, y, Skill.PRAYER, v.getPrayer(), v.getMaxPrayer(),
			v.getPrayer() + " / " + v.getMaxPrayer(), PRAYER_COLOR);
		y += ROW_H + ROW_GAP;

		drawRow(g, y, Skill.AGILITY, v.getRunEnergy(), 100,
			v.getRunEnergy() + "%" + (v.isStaminaActive() ? "  Stamina" : ""), v.isStaminaActive() ? STAMINA_COLOR : RUN_COLOR);
	}

	private void drawRow(Graphics2D g, int y, Skill iconSkill, int value, int max, String text, Color color)
	{
		int x = PADDING;
		int w = WIDTH - PADDING * 2;
		drawSlot(g, x, y, w, ROW_H);

		BufferedImage icon = skillIconManager.getSkillImage(iconSkill, false);
		if (icon != null)
		{
			drawIcon(g, icon, x + 6, y + (ROW_H - ICON) / 2, ICON);
		}

		int barX = x + 6 + ICON + 6;
		int barW = x + w - 6 - barX;
		int barY = y + (ROW_H - BAR_H) / 2;
		// Boosted levels (e.g. overheal) can exceed the max, so clamp the fill
		Draw.bar(g, barX, barY, barW, BAR_H, max > 0 ? value / (double) max : 0, color);
		g.setColor(SLOT_BORDER);
		g.drawRect(barX, barY, barW - 1, BAR_H - 1);

		g.setFont(FontManager.getRunescapeSmallFont());
		int textW = g.getFontMetrics().stringWidth(text);
		Draw.shadowed(g, text, barX + (barW - textW) / 2, barY + 13, Color.WHITE);
	}
}
