package com.upgradefinder;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class UpgradeFinderPluginTest // <-- This class name has been corrected
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(UpgradeFinderPlugin.class);
        RuneLite.main(args);
    }
}