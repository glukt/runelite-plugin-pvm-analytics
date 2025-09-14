package com.upgradefinder;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Provides;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.ClientTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
        name = "Upgrade Finder"
)
public class UpgradeFinderPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private UpgradeFinderConfig config;

    @Inject
    private TooltipManager tooltipManager;

    @Inject
    private Gson gson;

    private JsonObject progressionDatabase;

    @Override
    protected void startUp() throws Exception {
        loadProgressionDatabase();
    }

    private void loadProgressionDatabase() {
        try (InputStream in = getClass().getResourceAsStream("/progression_database.json")) {
            progressionDatabase = gson.fromJson(new InputStreamReader(in), JsonObject.class);
        } catch (Exception e) {
            log.error("Error loading progression database", e);
        }
    }

    @Subscribe
    public void onClientTick(ClientTick clientTick) {
        if (!config.enablePlugin() || client.isMenuOpen() || !config.modifierKey().isPressed()) {
            return;
        }

        MenuEntry[] menuEntries = client.getMenuEntries();
        if (menuEntries.length == 0) {
            return;
        }

        MenuEntry lastEntry = menuEntries[menuEntries.length - 1];
        if (lastEntry.getType() != MenuAction.ITEM_ON_WIDGET || !lastEntry.getOption().equals("Examine")) {
            return;
        }

        // We have a hovered item, now let's show our tooltip
        showUpgradeTooltip(lastEntry.getItemId());
    }

    private void showUpgradeTooltip(int itemId) {
        // Here you will look up the item in your progression database
        // and build the tooltip string. This is a simplified example.
        // In a real implementation, you would parse the JSON and check
        // the player's stats and quest progress.

        String tooltipText = "Upgrade information for item: " + itemId; // Placeholder

        tooltipManager.add(new Tooltip(tooltipText));
    }

    @Provides
    UpgradeFinderConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(UpgradeFinderConfig.class);
    }
}