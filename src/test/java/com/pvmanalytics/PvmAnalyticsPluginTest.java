package com.pvmanalytics;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PvmAnalyticsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PvmAnalyticsPlugin.class);
		RuneLite.main(args);
	}
}