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
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
        name = "Upgrade Finder"
)
public class UpgradeFinderPlugin extends Plugin
{
    private static final String UPGRADE_FINDER_ACTION = "Check Upgrades";

    @Inject
    private Client client;

    @Inject
    private UpgradeFinderConfig config;

    @Inject
    private Gson gson;

    private JsonObject progressionDatabase;

    @Override
    protected void startUp() throws Exception
    {
        loadProgressionDatabase();
    }

    private void loadProgressionDatabase()
    {
        try (InputStream in = getClass().getResourceAsStream("/progression_database.json"))
        {
            progressionDatabase = gson.fromJson(new InputStreamReader(in), JsonObject.class);
        }
        catch (Exception e)
        {
            log.error("Error loading progression database", e);
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        // This event fires every time a new option is added to the right-click menu.
        // We check if the option is an item in the inventory or equipment screen.
        final String option = Text.removeTags(event.getOption()).toLowerCase();
        if (option.contains("wear") || option.contains("wield") || option.contains("remove"))
        {
            client.createMenuEntry(-1)
                    .setOption(UPGRADE_FINDER_ACTION)
                    .setTarget(event.getTarget())
                    .setType(MenuAction.RUNELITE)
                    .setIdentifier(event.getIdentifier());
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        // This event fires when a menu option is clicked.
        // We check if it's our custom option.
        if (event.getMenuOption().equals(UPGRADE_FINDER_ACTION))
        {
            // The item ID is stored in the identifier field for item options.
            int itemId = event.getId();
            showUpgradeInfoInChat(itemId);
        }
    }

    private void showUpgradeInfoInChat(int itemId)
    {
        if (progressionDatabase == null)
        {
            return;
        }

        JsonArray amuletUpgrades = progressionDatabase.getAsJsonArray("AMULET");
        if (amuletUpgrades == null)
        {
            return;
        }

        for (JsonElement itemElement : amuletUpgrades)
        {
            JsonObject itemObject = itemElement.getAsJsonObject();
            if (itemObject.get("itemId").getAsInt() == itemId)
            {
                StringBuilder chatMessage = new StringBuilder();
                chatMessage.append("Upgrade Path for ").append(itemObject.get("itemName").getAsString()).append(":");

                JsonArray nextUpgrades = itemObject.getAsJsonArray("nextUpgrades");
                for (JsonElement upgradeElement : nextUpgrades)
                {
                    JsonObject upgradeObject = upgradeElement.getAsJsonObject();
                    chatMessage.append("<br>→ ").append(upgradeObject.get("itemName").getAsString());

                    JsonArray requirements = upgradeObject.getAsJsonArray("requirements");
                    for (JsonElement reqElement : requirements)
                    {
                        JsonObject reqObject = reqElement.getAsJsonObject();
                        String type = reqObject.get("type").getAsString();

                        if ("SKILL".equals(type))
                        {
                            String skillName = reqObject.get("name").getAsString();
                            int requiredLevel = reqObject.get("level").getAsInt();
                            int currentLevel = client.getRealSkillLevel(Skill.valueOf(skillName));

                            String colorHex = currentLevel >= requiredLevel ? "00ff00" : "ff0000"; // Green or Red
                            chatMessage.append("<br>  └ ")
                                    .append("<col=").append(colorHex).append(">")
                                    .append(requiredLevel).append(" ").append(skillName)
                                    .append("</col>");
                        }
                    }
                }

                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", chatMessage.toString(), null);
                return;
            }
        }
        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "No upgrade path found for this item.", null);
    }


    @Provides
    UpgradeFinderConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(UpgradeFinderConfig.class);
    }
}