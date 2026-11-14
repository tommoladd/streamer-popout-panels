package com.streamerpopoutpanels.ui;

import java.awt.Graphics2D;
import javax.swing.Timer;
import net.runelite.client.ui.FontManager;

/**
 * Base for list-style views. These scale to the window width only and show as many rows as
 * fit in the window height. Also supports fading highlights on new entries.
 */
public abstract class ListView extends PopoutView
{
	protected static final int WIDTH = 300;
	protected static final int PADDING = 6;
	protected static final int ROW_GAP = 4;
	protected static final int ROW_W = WIDTH - PADDING * 2;

	/**
	 * Drives the highlight fade; only runs while something is highlighted.
	 */
	private final Timer fadeTimer;
	private long highlightMillis;

	protected ListView()
	{
		fadeTimer = new Timer(50, e ->
		{
			repaint();
			if (!anyHighlighted())
			{
				((Timer) e.getSource()).stop();
			}
		});
	}

	@Override
	protected int naturalWidth()
	{
		return WIDTH;
	}

	/**
	 * Follows the window's aspect ratio, so the base class ends up scaling to the window width.
	 */
	@Override
	protected int naturalHeight()
	{
		return Math.max(1, getHeight() * WIDTH / Math.max(1, getWidth()));
	}

	public void setHighlightSeconds(int seconds)
	{
		highlightMillis = seconds * 1000L;
		contentChanged();
	}

	@Override
	public void stop()
	{
		fadeTimer.stop();
	}

	/**
	 * Call after the view's data changes.
	 */
	protected void contentChanged()
	{
		if (anyHighlighted())
		{
			fadeTimer.start();
		}
		repaint();
	}

	/**
	 * @return whether any entry currently has a highlight
	 */
	protected boolean anyHighlighted()
	{
		return false;
	}

	/**
	 * @return highlight strength from 1 (brand new) fading to 0
	 */
	protected float highlightStrength(long timestamp)
	{
		long age = System.currentTimeMillis() - timestamp;
		if (highlightMillis <= 0 || age < 0 || age >= highlightMillis)
		{
			return 0;
		}
		return 1f - age / (float) highlightMillis;
	}

	protected static void drawHighlight(Graphics2D g, int x, int y, int w, int h, float strength)
	{
		if (strength <= 0)
		{
			return;
		}
		g.setColor(Draw.withAlpha(Draw.GOLD, (int) (70 * strength)));
		g.fillRoundRect(x, y, w, h, 6, 6);
		g.setColor(Draw.withAlpha(Draw.GOLD, (int) (255 * strength)));
		g.drawRoundRect(x, y, w - 1, h - 1, 6, 6);
	}

	protected static void drawEmpty(Graphics2D g, String text)
	{
		g.setFont(FontManager.getRunescapeFont());
		g.setColor(Draw.MUTED);
		g.drawString(text, (WIDTH - g.getFontMetrics().stringWidth(text)) / 2, PADDING + 22);
	}
}
