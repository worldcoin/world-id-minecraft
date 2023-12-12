package org.worldcoin.bukkit.plugin.worldid;

import org.bukkit.plugin.java.JavaPlugin;
import org.worldcoin.bukkit.plugin.worldid.commands.VerifyCommand;
import org.worldcoin.bukkit.plugin.worldid.event.JoinListener;

public class WorldId extends JavaPlugin {

    public String orbGroupName = this.getConfig().getString("orb-group-name");
    public String deviceGroupName = this.getConfig().getString("device-group-name");
    public String appId = this.getConfig().getString("worldcoin-app-id");

    private boolean isConfigured() {
        if (appId == null) {
            getLogger().warning("You must configure the Worldcoin App ID in config.yml file before using this plugin!");
            return false;
        }
        if (orbGroupName == null) {
            getLogger().warning("You must configure an Orb Group Name in config.yml file before using this plugin!");
            return false;
        }
        if (deviceGroupName == null) {
            getLogger().warning("You have not configured a Device Group Name. Only Orb-verified World ID users will be able to verify.");
        }
        return true;
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        getLogger().info("Initialized the config.");
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