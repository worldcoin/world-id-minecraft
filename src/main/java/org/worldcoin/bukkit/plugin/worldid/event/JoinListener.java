package org.worldcoin.bukkit.plugin.worldid.event;

import org.worldcoin.bukkit.plugin.worldid.WorldId;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private WorldId plugin = WorldId.getPlugin(WorldId.class);

    public JoinListener(WorldId plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage("Welcome to the server!");
        if (player.hasPermission("group."+plugin.groupName)) {
            player.sendMessage("You've been verified with World ID!");
        } else {
            player.sendMessage("You haven't been verified with World ID!");
            player.sendMessage("You won't be able to interact with the world until you use the `/verify` command.");
        }
    }
}
