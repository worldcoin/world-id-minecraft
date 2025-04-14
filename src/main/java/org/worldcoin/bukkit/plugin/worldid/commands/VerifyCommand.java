package org.worldcoin.bukkit.plugin.worldid.commands;

import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.worldcoin.bukkit.plugin.worldid.WorldIdPlugin;
import org.worldcoin.bukkit.plugin.worldid.config.WorldIdSettings;
import org.worldcoin.bukkit.plugin.worldid.tasks.CheckVerified;

public class VerifyCommand implements CommandExecutor {

    private WorldIdPlugin plugin;

    private final String baseUrl;
    private final String serverUUID;

    public VerifyCommand(WorldIdPlugin plugin) {
        this.plugin = plugin;

        final WorldIdSettings settings = plugin.getSettings();
        this.baseUrl = settings.getWebUrl();
        this.serverUUID = settings.getUuid();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player!");
            return true;
        }

        final Player player = (Player) sender;

        if (this.plugin.isVerified(player)) {
            player.sendMessage("You've already been verified with World ID!");
            return true;
        }

        final UUID reqUUID = UUID.randomUUID();
        final String url = this.baseUrl + "/verify?reqUUID=" + reqUUID + "&serverUUID=" + serverUUID;

        player.sendMessage("Click here to verify with World ID:");
        player.sendMessage("");

        // Potential improvement: Use some sort of parser (MiniMessage) that allows configurable complex formatting
        final TextComponent button = new TextComponent("Verify!");
        button.setColor(ChatColor.WHITE);
        button.setBold(true);
        button.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

        player.spigot().sendMessage(button);
        player.sendMessage("");

        new CheckVerified(plugin, player, reqUUID, 20).runTaskTimerAsynchronously(plugin, 100, 200);
        return true;

    }
}
