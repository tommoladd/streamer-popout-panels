package com.streamerpopoutpanels.ui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.Locale;

/**
 * Drawing and formatting helpers shared by the popout views.
 */
public final class Draw
{
	public static final Color YELLOW = new Color(255, 255, 0);
	public static final Color GOLD = new Color(255, 200, 0);
	public static final Color GREEN = new Color(0, 255, 0);
	public static final Color RED = new Color(255, 70, 70);
	public static final Color MUTED = new Color(255, 255, 255, 140);
	private static final Color BAR_BACKGROUND = new Color(0, 0, 0, 120);

	private Draw()
	{
	}

	public static void shadowed(Graphics2D g, String text, int x, int y, Color color)
	{
		g.setColor(Color.BLACK);
		g.drawString(text, x + 1, y + 1);
		g.setColor(color);
		g.drawString(text, x, y);
	}

	public static void shadowedRight(Graphics2D g, String text, int right, int y, Color color)
	{
		shadowed(g, text, right - g.getFontMetrics().stringWidth(text), y, color);
	}

	public static void bar(Graphics2D g, int x, int y, int w, int h, double progress, Color color)
	{
		g.setColor(BAR_BACKGROUND);
		g.fillRect(x, y, w, h);
		int filled = (int) Math.round(w * Math.max(0, Math.min(1, progress)));
		g.setColor(color);
		g.fillRect(x, y, filled, h);
	}

	public static String truncate(String text, FontMetrics fm, int width)
	{
		if (fm.stringWidth(text) <= width)
		{
			return text;
		}
		String ellipsis = "...";
		int end = text.length();
		while (end > 0 && fm.stringWidth(text.substring(0, end) + ellipsis) > width)
		{
			end--;
		}
		return text.substring(0, end) + ellipsis;
	}

	public static Color withAlpha(Color c, int alpha)
	{
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, alpha)));
	}

	/**
	 * 1234567 -> "1,234,567"
	 */
	public static String number(long value)
	{
		return String.format(Locale.ENGLISH, "%,d", value);
	}

	/**
	 * 1234567 -> "1.2M"
	 */
	public static String shortNumber(long value)
	{
		long abs = Math.abs(value);
		if (abs < 10_000)
		{
			return number(value);
		}
		if (abs < 1_000_000)
		{
			return String.format(Locale.ENGLISH, "%.1fK", value / 1_000d);
		}
		if (abs < 1_000_000_000)
		{
			return String.format(Locale.ENGLISH, "%.1fM", value / 1_000_000d);
		}
		return String.format(Locale.ENGLISH, "%.2fB", value / 1_000_000_000d);
	}

	/**
	 * 3725 -> "1:02:05"
	 */
	public static String duration(long seconds)
	{
		long h = seconds / 3600;
		long m = (seconds % 3600) / 60;
		long s = seconds % 60;
		return h > 0
			? String.format(Locale.ENGLISH, "%d:%02d:%02d", h, m, s)
			: String.format(Locale.ENGLISH, "%d:%02d", m, s);
	}
}
