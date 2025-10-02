package com.upgradefinder;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("upgradefinder")
public interface UpgradeFinderConfig extends Config
{
    @ConfigItem(
            keyName = "enablePlugin",
            name = "Enable Upgrade Finder",
            description = "Toggles the plugin on or off"
    )
    default boolean enablePlugin()
    {
        return true;
    }
}