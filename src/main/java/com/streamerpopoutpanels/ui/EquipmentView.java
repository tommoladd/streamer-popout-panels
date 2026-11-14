package com.streamerpopoutpanels.ui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import net.runelite.api.EquipmentInventorySlot;

public class EquipmentView extends PopoutView
{
	private static final int CELL = 40;
	private static final int GAP = 8;
	private static final int PADDING = 8;

	/**
	 * Slot layout matching the in-game worn equipment tab, as {column, row}.
	 */
	private static final Map<EquipmentInventorySlot, int[]> LAYOUT = new EnumMap<>(EquipmentInventorySlot.class);

	static
	{
		LAYOUT.put(EquipmentInventorySlot.HEAD, new int[]{1, 0});
		LAYOUT.put(EquipmentInventorySlot.CAPE, new int[]{0, 1});
		LAYOUT.put(EquipmentInventorySlot.AMULET, new int[]{1, 1});
		LAYOUT.put(EquipmentInventorySlot.AMMO, new int[]{2, 1});
		LAYOUT.put(EquipmentInventorySlot.WEAPON, new int[]{0, 2});
		LAYOUT.put(EquipmentInventorySlot.BODY, new int[]{1, 2});
		LAYOUT.put(EquipmentInventorySlot.SHIELD, new int[]{2, 2});
		LAYOUT.put(EquipmentInventorySlot.LEGS, new int[]{1, 3});
		LAYOUT.put(EquipmentInventorySlot.GLOVES, new int[]{0, 4});
		LAYOUT.put(EquipmentInventorySlot.BOOTS, new int[]{1, 4});
		LAYOUT.put(EquipmentInventorySlot.RING, new int[]{2, 4});
	}

	public static Iterable<EquipmentInventorySlot> displayedSlots()
	{
		return LAYOUT.keySet();
	}

	private Map<EquipmentInventorySlot, BufferedImage> items = new EnumMap<>(EquipmentInventorySlot.class);

	/**
	 * @param items item image per worn slot; missing slots are empty
	 */
	public void setItems(Map<EquipmentInventorySlot, BufferedImage> items)
	{
		this.items = items;
		items.values().forEach(this::repaintWhenLoaded);
		repaint();
	}

	@Override
	protected int naturalWidth()
	{
		return 3 * CELL + 2 * GAP + PADDING * 2;
	}

	@Override
	protected int naturalHeight()
	{
		return 5 * CELL + 4 * GAP + PADDING * 2;
	}

	@Override
	protected void paintContent(Graphics2D g)
	{
		for (Map.Entry<EquipmentInventorySlot, int[]> entry : LAYOUT.entrySet())
		{
			int x = PADDING + entry.getValue()[0] * (CELL + GAP);
			int y = PADDING + entry.getValue()[1] * (CELL + GAP);
			drawSlot(g, x, y, CELL, CELL);

			BufferedImage image = items.get(entry.getKey());
			if (image != null)
			{
				drawCentered(g, image, x, y, CELL, CELL);
			}
		}
	}
}
