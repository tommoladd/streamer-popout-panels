package com.streamerpopoutpanels.ui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class InventoryView extends PopoutView
{
	public static final int SLOTS = 28;
	private static final int COLUMNS = 4;
	private static final int ROWS = 7;
	private static final int CELL_W = 42;
	private static final int CELL_H = 36;
	private static final int PADDING = 6;

	private BufferedImage[] items = new BufferedImage[SLOTS];

	/**
	 * @param items item images per inventory slot, null for empty slots
	 */
	public void setItems(BufferedImage[] items)
	{
		this.items = items;
		for (BufferedImage image : items)
		{
			repaintWhenLoaded(image);
		}
		repaint();
	}

	@Override
	protected int naturalWidth()
	{
		return COLUMNS * CELL_W + PADDING * 2;
	}

	@Override
	protected int naturalHeight()
	{
		return ROWS * CELL_H + PADDING * 2;
	}

	@Override
	protected void paintContent(Graphics2D g)
	{
		for (int i = 0; i < items.length && i < SLOTS; i++)
		{
			BufferedImage image = items[i];
			if (image != null)
			{
				int x = PADDING + (i % COLUMNS) * CELL_W;
				int y = PADDING + (i / COLUMNS) * CELL_H;
				drawCentered(g, image, x, y, CELL_W, CELL_H);
			}
		}
	}
}
