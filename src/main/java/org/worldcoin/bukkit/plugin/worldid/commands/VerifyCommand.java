package org.worldcoin.bukkit.plugin.worldid.commands;

import java.util.UUID;

import org.worldcoin.bukkit.plugin.worldid.WorldId;
import org.worldcoin.bukkit.plugin.worldid.tasks.CheckVerified;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class VerifyCommand implements CommandExecutor {

    private WorldId plugin = WorldId.getPlugin(WorldId.class);

    private FileConfiguration config = plugin.getConfig();
    private String orbGroupName = config.getString("orb-group-name");
    private String deviceGroupName = config.getString("device-group-name");
    private String baseUrl = config.getString("web-url");
    private String serverUUID = config.getString("server-uuid");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (player.hasPermission("group." + orbGroupName) | player.hasPermission("group." + deviceGroupName)) {
                player.sendMessage("You've already been verified with World ID!");
                return true;
            }
            UUID reqUUID = UUID.randomUUID();
            String url = baseUrl + "/verify?reqUUID=" + reqUUID + "&serverUUID=" + serverUUID;

            player.sendMessage("Click here to verify with World ID:");
            player.sendMessage("");

            TextComponent button = new TextComponent("Verify!");
            button.setColor(ChatColor.WHITE);
            button.setBold(true);
            button.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

            player.spigot().sendMessage( button );
            player.sendMessage("");

            new CheckVerified(player, reqUUID, 20).runTaskTimerAsynchronously(plugin, 100, 200);
            return true;
        } else {
            sender.sendMessage("You must be a player!");
            return false;
        }
    }
}
