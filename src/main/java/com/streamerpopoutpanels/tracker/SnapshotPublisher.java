package com.streamerpopoutpanels.tracker;

import java.util.function.Consumer;
import javax.swing.SwingUtilities;

/**
 * Trackers keep their state on the client thread and hand immutable snapshots of it to the
 * Swing event dispatch thread through this.
 */
public abstract class SnapshotPublisher<T>
{
	private volatile Consumer<T> listener;

	/**
	 * @param listener called on the event dispatch thread with each new snapshot
	 */
	public void setListener(Consumer<T> listener)
	{
		this.listener = listener;
	}

	protected void publish(T snapshot)
	{
		Consumer<T> l = listener;
		if (l != null)
		{
			SwingUtilities.invokeLater(() -> l.accept(snapshot));
		}
	}
}
