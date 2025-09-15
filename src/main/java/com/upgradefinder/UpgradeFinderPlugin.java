package com.upgradefinder;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Provides;
import java.awt.Color;
import java.io.IOException;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

    @Inject
    private OkHttpClient okHttpClient;

    @Inject
    private ClientThread clientThread;

    @Override
    protected void startUp() throws Exception
    {
        log.info("Upgrade Finder started!");
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
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
        if (event.getMenuOption().equals(UPGRADE_FINDER_ACTION))
        {
            int itemId = event.getId();
            String itemName = client.getItemDefinition(itemId).getName();
            fetchUpgradeInfoFromWiki(itemName);
        }
    }

    private void fetchUpgradeInfoFromWiki(String itemName)
    {
        sendChatMessage("Looking up upgrade path for " + itemName + "...");

        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("oldschool.runescape.wiki")
                .addPathSegment("api.php")
                .addQueryParameter("action", "parse")
                .addQueryParameter("page", "Jewellery")
                .addQueryParameter("prop", "text")
                .addQueryParameter("format", "json")
                .build();

        Request request = new Request.Builder().url(url).build();

        okHttpClient.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                log.error("Failed to fetch upgrade info from wiki", e);
                clientThread.invokeLater(() -> sendChatMessage("Error looking up upgrade path."));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                if (!response.isSuccessful())
                {
                    log.error("Unexpected code " + response);
                    clientThread.invokeLater(() -> sendChatMessage("Error looking up upgrade path: " + response.message()));
                    response.close();
                    return;
                }

                try (response)
                {
                    final String jsonResponse = response.body().string();
                    clientThread.invokeLater(() -> {
                        try
                        {
                            JsonObject json = gson.fromJson(jsonResponse, JsonObject.class);
                            String htmlContent = json.getAsJsonObject("parse").getAsJsonObject("text").get("*").getAsString();
                            Document doc = Jsoup.parse(htmlContent);

                            parseAmuletTable(doc, itemName);
                        }
                        catch (Exception e)
                        {
                            log.error("Error parsing wiki response", e);
                            sendChatMessage("Failed to parse wiki response.");
                        }
                    });
                }
            }
        });
    }

    private void parseAmuletTable(Document doc, String currentItemName)
    {
        Element amuletHeader = doc.select("h2:has(span#Amulets)").first();
        if (amuletHeader == null)
        {
            sendChatMessage("Could not find the Amulets section on the wiki page.");
            return;
        }

        Element amuletTable = amuletHeader.nextElementSibling();
        if (amuletTable == null || !amuletTable.tagName().equals("table"))
        {
            sendChatMessage("Could not find the Amulets table.");
            return;
        }

        Elements rows = amuletTable.select("tr");
        boolean foundCurrentItem = false;

        // Handle charged glory case by normalizing the name
        String normalizedCurrentItemName = currentItemName;
        if (normalizedCurrentItemName.toLowerCase().startsWith("amulet of glory"))
        {
            normalizedCurrentItemName = "Amulet of glory";
        }


        for (Element row : rows)
        {
            Elements cells = row.select("td");
            if (cells.size() > 1)
            {
                String itemNameInRow = cells.get(0).text();

                if (foundCurrentItem)
                {
                    // Look for the next item that has a clear crafting or quest requirement
                    String requirements = cells.get(1).text();
                    if (requirements.toLowerCase().contains("crafting") || requirements.toLowerCase().contains("quest"))
                    {
                        String upgradeName = cells.get(0).text();
                        sendChatMessage("Next upgrade for " + ColorUtil.wrapWithColorTag(currentItemName, Color.YELLOW)
                                + " is " + ColorUtil.wrapWithColorTag(upgradeName, Color.GREEN)
                                + ". Requirements: " + requirements);
                        return;
                    }
                }

                if (itemNameInRow.equalsIgnoreCase(normalizedCurrentItemName))
                {
                    foundCurrentItem = true;
                }
            }
        }

        if (foundCurrentItem)
        {
            sendChatMessage("This appears to be the final upgrade in its path.");
        }
        else
        {
            sendChatMessage("No upgrade path found for this item in the Jewellery wiki table.");
        }
    }


    private void sendChatMessage(String message)
    {
        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
    }

    @Provides
    UpgradeFinderConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(UpgradeFinderConfig.class);
    }
}