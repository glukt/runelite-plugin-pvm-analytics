package com.pvmanalytics;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.Arrays;

@Slf4j
@PluginDescriptor(
        name = "PvM Analytics"
)
public class PvmAnalyticsPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private PvmAnalyticsConfig config;

    private static final int GAUNTLET_LOBBY_REGION_ID = 12127;
    private static final int CORRUPTED_GAUNTLET_LOBBY_REGION_ID = 7512; // Example ID, adjust if needed
    private boolean inGauntletLobby = false;

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
    public void onGameStateChanged(GameStateChanged gameStateChanged)
    {
        if (gameStateChanged.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }

        boolean isInGauntletRegion = isPlayerInGauntletLobby();

        if (isInGauntletRegion && !inGauntletLobby)
        {
            log.info("Gauntlet trip started!");
            inGauntletLobby = true;
        }
        else if (!isInGauntletRegion && inGauntletLobby)
        {
            log.info("Gauntlet trip ended!");
            inGauntletLobby = false;
        }
    }

    private boolean isPlayerInGauntletLobby()
    {
        int[] mapRegions = client.getMapRegions();
        return Arrays.stream(mapRegions).anyMatch(id ->
                id == GAUNTLET_LOBBY_REGION_ID || id == CORRUPTED_GAUNTLET_LOBBY_REGION_ID);
    }


    @Provides
    PvmAnalyticsConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(PvmAnalyticsConfig.class);
    }
}