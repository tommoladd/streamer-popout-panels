package com.streamerpopoutpanels.ui;

import com.streamerpopoutpanels.PanelType;
import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * A standalone window holding one popout view, meant to be captured by OBS "Window Capture".
 */
public class PopoutWindow extends JFrame
{
	public PopoutWindow(PanelType type, PopoutView view, Rectangle savedBounds, boolean alwaysOnTop,
		Runnable onUserClose, Consumer<Rectangle> onBoundsChanged)
	{
		super(type.getWindowTitle());
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setLayout(new BorderLayout());
		add(view, BorderLayout.CENTER);
		setAlwaysOnTop(alwaysOnTop);

		if (savedBounds != null && isOnScreen(savedBounds))
		{
			setBounds(savedBounds);
		}
		else
		{
			setSize(type.getDefaultWidth(), type.getDefaultHeight());
			setLocationByPlatform(true);
		}

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				onUserClose.run();
			}
		});

		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentMoved(ComponentEvent e)
			{
				reportBounds(onBoundsChanged);
			}

			@Override
			public void componentResized(ComponentEvent e)
			{
				reportBounds(onBoundsChanged);
			}
		});
	}

	private void reportBounds(Consumer<Rectangle> onBoundsChanged)
	{
		// Minimised windows are moved far off screen, so only remember normal bounds
		if (isVisible() && getExtendedState() == NORMAL)
		{
			onBoundsChanged.accept(getBounds());
		}
	}

	private static boolean isOnScreen(Rectangle bounds)
	{
		for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices())
		{
			if (device.getDefaultConfiguration().getBounds().intersects(bounds))
			{
				return true;
			}
		}
		return false;
	}
}
