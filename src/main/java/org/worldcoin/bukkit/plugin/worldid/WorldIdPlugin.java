package org.worldcoin.bukkit.plugin.worldid;

import com.posthog.java.PostHog;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.worldcoin.bukkit.plugin.worldid.commands.VerifyCommand;
import org.worldcoin.bukkit.plugin.worldid.config.WorldIdSettings;
import org.worldcoin.bukkit.plugin.worldid.event.JoinListener;

public class WorldIdPlugin extends JavaPlugin {

    private WorldIdSettings settings;

    @Override
    public void onEnable() {
        this.settings = new WorldIdSettings(this);
        this.ensureUUID();

        final String serverUUID = settings.getUuid();
        final PostHog posthog = settings.getPosthog();

        this.log("Initialized the config.");
        this.log("Server UUID: " + serverUUID);

        if (!settings.isValid()) {
            getServer().getPluginManager().disablePlugin(this);
            getLogger().warning("Plugin disabled.");
            return;
        }

        this.log("Plugin enabled.");

        this.getCommand("verify").setExecutor(new VerifyCommand(this));
        this.log("Added the 'verify' command.");

        new JoinListener(this);
        this.log("Listening for player joins.");

        posthog.capture(serverUUID, "minecraft integration app started");

    }

    @Override
    public void onDisable() {
        final PostHog posthog = settings.getPosthog();
        final String serverUUID = settings.getUuid();

        posthog.capture(serverUUID, "minecraft integration app stopped");
        posthog.shutdown();
    }

    public boolean isVerified(Player player) {
        return player.hasPermission("group." + settings.getOrbGroupName()) || player.hasPermission("group." + settings.getDeviceGroupName());
    }

    private void ensureUUID() {
        final String currentId = settings.getUuid();
        final PostHog posthog = settings.getPosthog();

        if (currentId != null && !currentId.isEmpty()) { // CurrentId is set and not empty
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