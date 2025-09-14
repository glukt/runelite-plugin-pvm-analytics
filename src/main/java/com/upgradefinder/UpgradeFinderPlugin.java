package com.upgradefinder;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Provides;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Skill;
import net.runelite.api.events.ClientTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ColorUtil;

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
        if (lastEntry.getOpcode() != MenuAction.ITEM_EXAMINE.getId())
        {
            return;
        }

        showUpgradeTooltip(lastEntry.getItemId());
    }

    private void showUpgradeTooltip(int itemId) {
        if (progressionDatabase == null) {
            return;
        }

        // We will improve this later to handle all slots
        JsonArray amuletUpgrades = progressionDatabase.getAsJsonArray("AMULET");
        if (amuletUpgrades == null) {
            return;
        }

        for (JsonElement itemElement : amuletUpgrades) {
            JsonObject itemObject = itemElement.getAsJsonObject();
            if (itemObject.get("itemId").getAsInt() == itemId) {
                // We've found the hovered item in our database
                StringBuilder tooltipText = new StringBuilder();
                tooltipText.append(ColorUtil.wrapWithColorTag("Next Upgrade:", java.awt.Color.YELLOW));

                JsonArray nextUpgrades = itemObject.getAsJsonArray("nextUpgrades");
                for (JsonElement upgradeElement : nextUpgrades) {
                    JsonObject upgradeObject = upgradeElement.getAsJsonObject();
                    tooltipText.append("<br>").append(upgradeObject.get("itemName").getAsString());

                    JsonArray requirements = upgradeObject.getAsJsonArray("requirements");
                    for (JsonElement reqElement : requirements) {
                        JsonObject reqObject = reqElement.getAsJsonObject();
                        String type = reqObject.get("type").getAsString();

                        if ("SKILL".equals(type)) {
                            String skillName = reqObject.get("name").getAsString();
                            int requiredLevel = reqObject.get("level").getAsInt();
                            int currentLevel = client.getRealSkillLevel(Skill.valueOf(skillName));

                            java.awt.Color color = currentLevel >= requiredLevel ? java.awt.Color.GREEN : java.awt.Color.RED;
                            tooltipText.append("<br>  └ ")
                                    .append(ColorUtil.wrapWithColorTag(requiredLevel + " " + skillName, color));
                        }
                        // We will add more requirement types (QUEST, etc.) here later
                    }
                }
                tooltipManager.add(new Tooltip(tooltipText.toString()));
                return;
            }
        }
    }


    @Provides
    UpgradeFinderConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(UpgradeFinderConfig.class);
    }
}