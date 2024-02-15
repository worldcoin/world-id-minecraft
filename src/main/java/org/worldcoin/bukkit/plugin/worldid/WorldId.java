package org.worldcoin.bukkit.plugin.worldid;

import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.worldcoin.bukkit.plugin.worldid.commands.VerifyCommand;
import org.worldcoin.bukkit.plugin.worldid.event.JoinListener;
import com.posthog.java.PostHog;

public class WorldId extends JavaPlugin {

    public static final String POSTHOG_API_KEY = "phc_QttqgDbMQDYHX1EMH7FnT6ECBVzdp0kGUq92aQaVQ6I";
    public static final String POSTHOG_HOST = "https://app.posthog.com";

    PostHog posthog = new PostHog.Builder(POSTHOG_API_KEY).host(POSTHOG_HOST).build();

    private boolean isConfigured(FileConfiguration config) {

        String orbGroupName = config.getString("orb-group-name");
        String deviceGroupName = config.getString("device-group-name");
        String uuid = config.getString("server-uuid");
        String webUrl = config.getString("web-url");

        if (uuid.equals("")) {
            getLogger().warning("You must configure a server UUID in config.yml file before using this plugin!");
            return false;
        }
        if (orbGroupName.equals("") && deviceGroupName.equals("")) {
            getLogger().warning(
                    "You have not configured an Orb Group Name or Device Group Name in config.yml. At least one group must be configured.");
            return false;
        }
        if (webUrl.equals("")) {
            getLogger().warning(
                    "You have not configured a Web URL in config.yml. This is https://minecraft.worldcoin.org by default. This must be configured.");
            return false;
        }
        if (orbGroupName.equals("")) {
            getLogger().info(
                    "You have not configured an Orb Group Name in config.yml. All World ID-verified users will be issued the same role regardless of their verification level.");
        }
        if (deviceGroupName.equals("")) {
            getLogger().info(
                    "You have not configured a Device Group Name in config.yml. Only Orb-verified World ID users will be able to verify.");
        }
        return true;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (getConfig().getString("server-uuid").equals("")) {
            getConfig().set("server-uuid", UUID.randomUUID().toString());
            saveConfig();
            getLogger().info(
                    "Set a new Server UUID. Do not change this value unless you are resetting all player verifications.");
            getLogger().info("Server UUID: " + getConfig().getString("server-uuid"));
            posthog.capture(getConfig().getString("server-uuid"), "minecraft integration app added");
        }
        getLogger().info("Initialized the config.");
        getLogger().info("Server UUID: " + getConfig().getString("server-uuid"));
        if (!isConfigured(getConfig())) {
            getServer().getPluginManager().disablePlugin(this);
            getLogger().warning("Plugin disabled.");
            return;
        } else {
            getLogger().info("Plugin enabled.");
            getCommand("verify").setExecutor(new VerifyCommand());
            getLogger().info("Added the 'verify' command.");
            new JoinListener(this);
            getLogger().info("Listening for player joins.");
            posthog.capture(getConfig().getString("server-uuid"), "minecraft integration app started");
        }
    }

    @Override
    public void onDisable() {
        posthog.capture(getConfig().getString("server-uuid"), "minecraft integration app stopped");
        posthog.shutdown();
    }
}