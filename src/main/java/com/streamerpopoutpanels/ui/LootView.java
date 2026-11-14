package com.streamerpopoutpanels.ui;

import com.streamerpopoutpanels.tracker.LootSession;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Collections;
import net.runelite.client.ui.FontManager;

public class LootView extends ListView
{
	private static final int HEADER_H = 36;
	private static final int LAST_H = 58;
	private static final int ITEM_W = 38;
	private static final int ROW_H = 38;

	private LootSession session = new LootSession(0, 0, null, Collections.emptyList());

	public void setSession(LootSession session)
	{
		this.session = session;
		if (session.getLastLoot() != null)
		{
			session.getLastLoot().getItems().forEach(item -> repaintWhenLoaded(item.getImage()));
		}
		session.getBestDrops().forEach(item -> repaintWhenLoaded(item.getImage()));
		repaint();
	}

	@Override
	protected void paintContent(Graphics2D g)
	{
		int x = PADDING;
		int y = PADDING;
		int right = x + ROW_W - 8;

		// Header: session total
		drawSlot(g, x, y, ROW_W, HEADER_H);
		g.setFont(FontManager.getRunescapeSmallFont());
		Draw.shadowed(g, "Session loot", x + 8, y + 14, Draw.MUTED);
		Draw.shadowedRight(g, session.getLootCount() + (session.getLootCount() == 1 ? " loot" : " loots"), right, y + 14, Draw.MUTED);
		g.setFont(FontManager.getRunescapeBoldFont());
		Draw.shadowed(g, Draw.shortNumber(session.getTotalValue()) + " gp", x + 8, y + 30, Draw.GOLD);
		y += HEADER_H + ROW_GAP;

		LootSession.Loot last = session.getLastLoot();
		if (last == null)
		{
			g.setFont(FontManager.getRunescapeFont());
			g.setColor(Draw.MUTED);
			String text = "No loot yet";
			g.drawString(text, (WIDTH - g.getFontMetrics().stringWidth(text)) / 2, y + 20);
			return;
		}

		// Most recent loot with its item icons
		if (y + LAST_H <= naturalHeight())
		{
			drawSlot(g, x, y, ROW_W, LAST_H);
			g.setFont(FontManager.getRunescapeSmallFont());
			String value = Draw.shortNumber(last.getValue()) + " gp";
			int valueW = g.getFontMetrics().stringWidth(value);
			Draw.shadowed(g, Draw.truncate("Last: " + last.getSource(), g.getFontMetrics(), ROW_W - valueW - 24), x + 8, y + 14, Color.WHITE);
			Draw.shadowedRight(g, value, right, y + 14, Draw.GOLD);

			int itemX = x + 4;
			for (LootSession.LootItem item : last.getItems())
			{
				if (itemX + ITEM_W > x + ROW_W)
				{
					break;
				}
				drawCentered(g, item.getImage(), itemX, y + 18, ITEM_W, 38);
				itemX += ITEM_W;
			}
			y += LAST_H + ROW_GAP;
		}

		if (session.getBestDrops().isEmpty() || y + 16 + ROW_H > naturalHeight())
		{
			return;
		}

		g.setFont(FontManager.getRunescapeSmallFont());
		Draw.shadowed(g, "Best drops", x + 2, y + 12, Draw.MUTED);
		y += 16;

		for (LootSession.LootItem item : session.getBestDrops())
		{
			if (y + ROW_H > naturalHeight())
			{
				break;
			}
			drawSlot(g, x, y, ROW_W, ROW_H);
			drawCentered(g, item.getImage(), x + 2, y + 1, 40, 36);

			int textX = x + 46;
			g.setFont(FontManager.getRunescapeSmallFont());
			String value = Draw.shortNumber(item.getValue()) + " gp";
			int valueW = g.getFontMetrics().stringWidth(value);
			String name = item.getQuantity() > 1 ? Draw.number(item.getQuantity()) + " x " + item.getName() : item.getName();
			Draw.shadowed(g, Draw.truncate(name, g.getFontMetrics(), right - textX - valueW - 6), textX, y + 16, Color.WHITE);
			Draw.shadowedRight(g, value, right, y + 16, Draw.GOLD);
			Draw.shadowed(g, Draw.truncate(item.getSource(), g.getFontMetrics(), right - textX), textX, y + 30, Draw.MUTED);

			y += ROW_H + ROW_GAP;
		}
	}
}
