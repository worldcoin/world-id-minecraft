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
import org.bukkit.entity.Player;

public class VerifyCommand implements CommandExecutor {

    private WorldId plugin = WorldId.getPlugin(WorldId.class);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (player.hasPermission("group." + plugin.orbGroupName) | player.hasPermission("group." + plugin.deviceGroupName)) {
                player.sendMessage("You've already been verified with World ID!");
                return true;
            }
            String webUrl = plugin.getConfig().getString("web-url");
            UUID uuid = UUID.randomUUID();
            String url = webUrl + "/verify?id=" + uuid + "&app_id=" + plugin.getConfig().getString("worldcoin-app-id");

            player.sendMessage("Click here to verify with World ID:");
            player.sendMessage("");

            TextComponent button = new TextComponent("Verify!");
            button.setColor(ChatColor.WHITE);
            button.setBold(true);
            button.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

            player.spigot().sendMessage( button );
            player.sendMessage("");

            new CheckVerified(player, uuid, webUrl, 20).runTaskTimer(plugin, 100, 200);
            return true;
        } else {
            sender.sendMessage("You must be a player!");
            return false;
        }
    }
}
