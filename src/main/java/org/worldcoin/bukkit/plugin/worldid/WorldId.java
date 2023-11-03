package org.worldcoin.bukkit.plugin.worldid;

import org.bukkit.plugin.java.JavaPlugin;
import org.worldcoin.bukkit.plugin.worldid.commands.VerifyCommand;
import org.worldcoin.bukkit.plugin.worldid.event.JoinListener;

public class WorldId extends JavaPlugin {

    public String groupName = this.getConfig().getString("verified-group-name");

    private boolean isConfigured() {
        if (this.getConfig().getString("worldcoin-app-id") == null) {
            getLogger().warning("You must configure the Worldcoin App ID in config.yml file before using this plugin!");
            return false;
        }
        return true;
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        getLogger().info("Initialized the config.");
        // configureWorldGuard();
        if (!isConfigured()) {
            this.getServer().getPluginManager().disablePlugin(this);
            getLogger().warning("Plugin disabled.");
            return;
        } else {
            getLogger().info("Plugin configured.");
            this.getCommand("verify").setExecutor(new VerifyCommand());
            getLogger().info("Added the 'verify' command.");
            new JoinListener(this);
            getLogger().info("Listening for player joins.");
        }
    }
}