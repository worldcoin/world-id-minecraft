package org.worldcoin.bukkit.plugin.worldid;

import java.util.UUID;

import org.bukkit.plugin.java.JavaPlugin;
import org.worldcoin.bukkit.plugin.worldid.commands.VerifyCommand;
import org.worldcoin.bukkit.plugin.worldid.event.JoinListener;

public class WorldId extends JavaPlugin {

    public String orbGroupName = this.getConfig().getString("orb-group-name");
    public String deviceGroupName = this.getConfig().getString("device-group-name");
    public String uuid = this.getConfig().getString("server-uuid");
    public String webUrl = this.getConfig().getString("web-url");

    private boolean isConfigured() {
        if (uuid == null) {
            getLogger().warning("You must configure a server UUID in config.yml file before using this plugin!");
            return false;
        }
        if (orbGroupName == null && deviceGroupName == null) {
            getLogger().warning("You have not configured an Orb Group Name or Device Group Name in config.yml. At least one group must be configured.");
            return false;
        }
        if (orbGroupName == null) {
            getLogger().warning("You have not configured an Orb Group Name in config.yml. All World ID-verified users will be issued the same role regardless of their verification level.");
        }
        if (deviceGroupName == null) {
            getLogger().warning("You have not configured a Device Group Name in config.yml. Only Orb-verified World ID users will be able to verify.");
        }
        return true;
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        if (this.getConfig().getString("server-uuid") == "") {
            this.getConfig().set("server-uuid", UUID.randomUUID().toString());
            this.saveConfig();
        }
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