package com.streamerpopoutpanels;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class StreamerPopoutPanelsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(StreamerPopoutPanelsPlugin.class);
		RuneLite.main(args);
	}
}
