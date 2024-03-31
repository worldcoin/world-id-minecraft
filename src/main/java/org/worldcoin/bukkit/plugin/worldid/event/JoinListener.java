package org.worldcoin.bukkit.plugin.worldid.event;

import org.worldcoin.bukkit.plugin.worldid.WorldIdPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private WorldIdPlugin plugin = WorldIdPlugin.getPlugin(WorldIdPlugin.class);

    private FileConfiguration config = plugin.getConfig();
    private String orbGroupName = config.getString("orb-group-name");
    private String deviceGroupName = config.getString("device-group-name");

    public JoinListener(WorldIdPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage("Welcome to the server!");
        if (player.hasPermission("group."+orbGroupName) || player.hasPermission("group."+deviceGroupName)) {
            player.sendMessage("You've been verified with World ID!");
        } else {
            player.sendMessage("You haven't been verified with World ID!");
            player.sendMessage("You can get permissions by typing the `/verify` command to verify with World ID.");
        }
    }
}
