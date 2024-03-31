package org.worldcoin.bukkit.plugin.worldid.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.worldcoin.bukkit.plugin.worldid.WorldIdPlugin;
import org.worldcoin.bukkit.plugin.worldid.config.WorldIdSettings;

public class JoinListener implements Listener {

    private final WorldIdPlugin plugin;

    public JoinListener(WorldIdPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        player.sendMessage("Welcome to the server!");

        if (this.plugin.isVerified(player)) {
            player.sendMessage("You've been verified with World ID!");
            return;
        }

        player.sendMessage("You haven't been verified with World ID!");
        player.sendMessage("You can get permissions by typing the `/verify` command to verify with World ID.");
    }
}
