package com.streamerpopoutpanels.ui;

import com.streamerpopoutpanels.tracker.BossRecord;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import net.runelite.client.ui.FontManager;

/**
 * Kill counts and personal bests, most recently killed first.
 */
public class BossLogView extends ListView
{
	private static final int ROW_H = 38;

	private List<BossRecord> records = new ArrayList<>();

	public void setRecords(List<BossRecord> records)
	{
		this.records = records;
		contentChanged();
	}

	@Override
	protected boolean anyHighlighted()
	{
		return records.stream().anyMatch(r -> highlightStrength(r.getPersonalBestSet()) > 0);
	}

	@Override
	protected void paintContent(Graphics2D g)
	{
		if (records.isEmpty())
		{
			drawEmpty(g, "No kills yet");
			return;
		}

		int x = PADDING;
		int y = PADDING;
		int right = x + ROW_W - 8;
		for (BossRecord record : records)
		{
			if (y + ROW_H > naturalHeight())
			{
				break;
			}
			drawSlot(g, x, y, ROW_W, ROW_H);
			float highlight = highlightStrength(record.getPersonalBestSet());
			drawHighlight(g, x, y, ROW_W, ROW_H, highlight);

			g.setFont(FontManager.getRunescapeFont());
			String kc = "KC " + Draw.number(record.getKillCount());
			int kcW = g.getFontMetrics().stringWidth(kc);
			Draw.shadowed(g, Draw.truncate(record.getName(), g.getFontMetrics(), ROW_W - kcW - 24), x + 8, y + 16, Color.WHITE);
			Draw.shadowedRight(g, kc, right, y + 16, Draw.YELLOW);

			if (record.getPersonalBest() != null)
			{
				g.setFont(FontManager.getRunescapeSmallFont());
				String pb = "PB " + record.getPersonalBest() + (highlight > 0 ? "  New personal best!" : "");
				Draw.shadowed(g, pb, x + 8, y + 31, highlight > 0 ? Draw.GOLD : Draw.MUTED);
			}

			y += ROW_H + ROW_GAP;
		}
	}
}
