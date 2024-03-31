package org.worldcoin.bukkit.plugin.worldid;

import com.posthog.java.PostHog;
import java.util.UUID;
import org.bukkit.plugin.java.JavaPlugin;
import org.worldcoin.bukkit.plugin.worldid.commands.VerifyCommand;
import org.worldcoin.bukkit.plugin.worldid.config.WorldIdSettings;
import org.worldcoin.bukkit.plugin.worldid.event.JoinListener;

public class WorldIdPlugin extends JavaPlugin {

    private WorldIdSettings settings;

    @Override
    public void onEnable() {
        this.settings = new WorldIdSettings(this);
        final PostHog posthog = settings.getPosthog();

        this.ensureUUID();
        final String serverUUID = settings.getUuid();

        log("Initialized the config.");
        log("Server UUID: " + settings.getUuid());

        if (!settings.isValid()) {
            getServer().getPluginManager().disablePlugin(this);
            getLogger().warning("Plugin disabled.");
            return;
        }

        log("Plugin enabled.");
        getCommand("verify").setExecutor(new VerifyCommand());
        log("Added the 'verify' command.");
        new JoinListener(this);
        log("Listening for player joins.");
        posthog.capture(serverUUID, "minecraft integration app started");

    }

    @Override
    public void onDisable() {
        final PostHog posthog = settings.getPosthog();
        final String serverUUID = settings.getUuid();

        posthog.capture(serverUUID, "minecraft integration app stopped");
        posthog.shutdown();
    }

    private void ensureUUID() {
        final String currentId = settings.getUuid();
        final PostHog posthog = settings.getPosthog();

        if (currentId != null && !currentId.isEmpty()) {
            return;
        }

        final String newId = UUID.randomUUID().toString();
        settings.setUuid(newId);
        settings.save();

        log("Set a new Server UUID. Do not change this value unless you are resetting all player verifications.");
        log("Server UUID: " + newId);
        posthog.capture(newId, "minecraft integration app added");
    }

    public WorldIdSettings getSettings() {
        return settings;
    }

    public void log(String message) {
        getLogger().info(message);
    }
}