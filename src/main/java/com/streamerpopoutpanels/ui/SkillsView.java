package com.streamerpopoutpanels.ui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import net.runelite.api.Skill;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.FontManager;

public class SkillsView extends PopoutView
{
	/**
	 * Skills in the order the in-game skills tab shows them, left to right, top to bottom.
	 */
	public static final Skill[] LAYOUT = {
		Skill.ATTACK, Skill.HITPOINTS, Skill.MINING,
		Skill.STRENGTH, Skill.AGILITY, Skill.SMITHING,
		Skill.DEFENCE, Skill.HERBLORE, Skill.FISHING,
		Skill.RANGED, Skill.THIEVING, Skill.COOKING,
		Skill.PRAYER, Skill.CRAFTING, Skill.FIREMAKING,
		Skill.MAGIC, Skill.FLETCHING, Skill.WOODCUTTING,
		Skill.RUNECRAFT, Skill.SLAYER, Skill.FARMING,
		Skill.CONSTRUCTION, Skill.HUNTER, Skill.SAILING,
	};

	private static final int COLUMNS = 3;
	private static final int CELL_W = 62;
	private static final int CELL_H = 32;
	private static final int GAP = 3;
	private static final int PADDING = 6;

	private final SkillIconManager skillIconManager;

	private Map<Skill, Integer> realLevels = new EnumMap<>(Skill.class);
	private Map<Skill, Integer> boostedLevels = new EnumMap<>(Skill.class);
	private int totalLevel;

	public SkillsView(SkillIconManager skillIconManager)
	{
		this.skillIconManager = skillIconManager;
	}

	public void setLevels(Map<Skill, Integer> realLevels, Map<Skill, Integer> boostedLevels, int totalLevel)
	{
		this.realLevels = realLevels;
		this.boostedLevels = boostedLevels;
		this.totalLevel = totalLevel;
		repaint();
	}

	private int rows()
	{
		return (LAYOUT.length + COLUMNS - 1) / COLUMNS;
	}

	@Override
	protected int naturalWidth()
	{
		return COLUMNS * CELL_W + (COLUMNS - 1) * GAP + PADDING * 2;
	}

	@Override
	protected int naturalHeight()
	{
		// One extra row for the total level
		int rows = rows() + 1;
		return rows * CELL_H + (rows - 1) * GAP + PADDING * 2;
	}

	@Override
	protected void paintContent(Graphics2D g)
	{
		g.setFont(FontManager.getRunescapeSmallFont());
		FontMetrics fm = g.getFontMetrics();

		for (int i = 0; i < LAYOUT.length; i++)
		{
			Skill skill = LAYOUT[i];
			int x = PADDING + (i % COLUMNS) * (CELL_W + GAP);
			int y = PADDING + (i / COLUMNS) * (CELL_H + GAP);
			drawSlot(g, x, y, CELL_W, CELL_H);

			BufferedImage icon = skillIconManager.getSkillImage(skill, true);
			if (icon != null)
			{
				drawCentered(g, icon, x + 2, y, 24, CELL_H);
			}

			int real = realLevels.getOrDefault(skill, 1);
			int boosted = boostedLevels.getOrDefault(skill, real);
			Color boostedColor = boosted > real ? Draw.GREEN : boosted < real ? Draw.RED : Draw.YELLOW;

			String boostedText = String.valueOf(boosted);
			String realText = String.valueOf(real);
			int textX = x + 30;
			Draw.shadowed(g, boostedText, textX + 14 - fm.stringWidth(boostedText) / 2, y + 14, boostedColor);
			Draw.shadowed(g, realText, textX + 22 - fm.stringWidth(realText) / 2, y + 27, Draw.YELLOW);
		}

		int totalY = PADDING + rows() * (CELL_H + GAP);
		int totalW = naturalWidth() - PADDING * 2;
		drawSlot(g, PADDING, totalY, totalW, CELL_H);
		g.setFont(FontManager.getRunescapeFont());
		fm = g.getFontMetrics();
		String total = "Total level: " + totalLevel;
		Draw.shadowed(g, total, PADDING + (totalW - fm.stringWidth(total)) / 2, totalY + (CELL_H + fm.getAscent()) / 2 - 2, Draw.YELLOW);
	}
}
