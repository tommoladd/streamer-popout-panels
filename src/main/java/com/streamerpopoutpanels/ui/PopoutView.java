package com.streamerpopoutpanels.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import net.runelite.client.util.AsyncBufferedImage;

/**
 * Base class for popout panel content. Subclasses draw at a fixed "natural" size and the
 * drawing is scaled to fit the window, so streamers can resize the window freely.
 * All methods must be called on the Swing event dispatch thread.
 */
public abstract class PopoutView extends JComponent
{
	protected static final Color SLOT_FILL = new Color(255, 255, 255, 18);
	protected static final Color SLOT_BORDER = new Color(255, 255, 255, 40);

	private Color backgroundColor = Color.BLACK;

	protected PopoutView()
	{
		setOpaque(true);
	}

	public void setBackgroundColor(Color color)
	{
		backgroundColor = color;
		repaint();
	}

	/**
	 * Adds an entry to the view's right-click menu.
	 */
	public void addMenuAction(String label, Runnable action)
	{
		JPopupMenu menu = getComponentPopupMenu();
		if (menu == null)
		{
			menu = new JPopupMenu();
			setComponentPopupMenu(menu);
		}
		JMenuItem item = new JMenuItem(label);
		item.addActionListener(e -> action.run());
		menu.add(item);
	}

	/**
	 * Stops any timers the view owns. Called when the plugin shuts down.
	 */
	public void stop()
	{
	}

	/**
	 * Size the content is drawn at before scaling.
	 */
	protected abstract int naturalWidth();

	protected abstract int naturalHeight();

	protected abstract void paintContent(Graphics2D g);

	@Override
	protected void paintComponent(Graphics graphics)
	{
		Graphics2D g = (Graphics2D) graphics.create();
		try
		{
			g.setColor(backgroundColor);
			g.fillRect(0, 0, getWidth(), getHeight());

			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

			double scale = Math.min(getWidth() / (double) naturalWidth(), getHeight() / (double) naturalHeight());
			if (scale <= 0)
			{
				return;
			}
			g.translate((getWidth() - naturalWidth() * scale) / 2, (getHeight() - naturalHeight() * scale) / 2);
			g.scale(scale, scale);
			paintContent(g);
		}
		finally
		{
			g.dispose();
		}
	}

	/**
	 * Repaint once an asynchronously loaded item image is ready.
	 */
	protected void repaintWhenLoaded(BufferedImage image)
	{
		if (image instanceof AsyncBufferedImage)
		{
			((AsyncBufferedImage) image).onLoaded(this::repaint);
		}
	}

	protected static void drawCentered(Graphics2D g, Image image, int x, int y, int w, int h)
	{
		g.drawImage(image, x + (w - image.getWidth(null)) / 2, y + (h - image.getHeight(null)) / 2, null);
	}

	/**
	 * Draws an image scaled to a square, smoothing it so small skill icons don't look jagged.
	 */
	protected static void drawIcon(Graphics2D g, Image image, int x, int y, int size)
	{
		Object old = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		double s = Math.min(size / (double) w, size / (double) h);
		int dw = (int) (w * s);
		int dh = (int) (h * s);
		g.drawImage(image, x + (size - dw) / 2, y + (size - dh) / 2, dw, dh, null);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, old);
	}

	protected static void drawSlot(Graphics2D g, int x, int y, int w, int h)
	{
		g.setColor(SLOT_FILL);
		g.fillRoundRect(x, y, w, h, 6, 6);
		g.setColor(SLOT_BORDER);
		g.drawRoundRect(x, y, w - 1, h - 1, 6, 6);
	}
}
