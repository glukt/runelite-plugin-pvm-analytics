package com.pvmanalytics;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
        name = "PvM Analytics"
)
public class PvmAnalyticsPlugin extends Plugin
{
    // A "Set" is a collection that holds unique values. We use it here to store all the valid Gauntlet region IDs.
    private static final Set<Integer> GAUNTLET_REGION_IDS = ImmutableSet.of(7512, 7768);

    @Inject
    private Client client;

    @Inject
    private PvmAnalyticsConfig config;

    // This variable will act as our memory, remembering if we are currently inside the instance.
    private boolean isInGauntletInstance = false;

    @Override
    protected void startUp() throws Exception
    {
        log.info("PvM Analytics started!");
    }

    @Override
    protected void shutDown() throws Exception
    {
        log.info("PvM Analytics stopped!");
    }

    @Subscribe
    public void onGameTick(GameTick gameTick)
    {
        // First, get the player's current location and find the region ID.
        int currentRegionId = client.getLocalPlayer().getWorldLocation().getRegionID();

        // Check if the player's current region ID is contained within our Set of Gauntlet IDs.
        if (GAUNTLET_REGION_IDS.contains(currentRegionId))
        {
            // If they are in the region, but our plugin doesn't know it yet, it means they have just entered.
            if (!isInGauntletInstance)
            {
                log.info("Gauntlet trip started!");
                // We update our state to remember that we are now inside.
                isInGauntletInstance = true;
            }
        }
        else
        {
            // If the player is not in a Gauntlet region, but our plugin thinks they are, it means they have just left.
            if (isInGauntletInstance)
            {
                log.info("Gauntlet trip ended!");
                // We update our state to remember that we are now outside.
                isInGauntletInstance = false;
            }
        }
    }

    @Provides
    PvmAnalyticsConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(PvmAnalyticsConfig.class);
    }
}