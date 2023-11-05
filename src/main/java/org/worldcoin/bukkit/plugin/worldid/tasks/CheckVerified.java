package org.worldcoin.bukkit.plugin.worldid.tasks;

import java.util.UUID;

import org.apache.hc.client5.http.fluent.Request;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.model.user.User;

public class CheckVerified extends BukkitRunnable {

    private final Player player;
    private final String groupName;
    private final UUID uuid;
    private final String url;
    private int counter;

    public CheckVerified(Player player, String groupName, UUID uuid, String url, int counter) {
        this.player = player;
        this.uuid = uuid;
        this.url = url;
        if (groupName == null) {
            throw new IllegalArgumentException("groupName cannot be null");
        } else {
            this.groupName = groupName;
        }
        if (counter <= 0) {
            throw new IllegalArgumentException("counter must be greater than 0");
        } else {
            this.counter = counter;
        }
        if (player.hasPermission("group." + groupName)) {
            throw new IllegalStateException("player is already verified");
        }
    }

    @Override
    public void run() {
        if (counter > 0) { 
            try {
                int responseCode = Request.get(url + "/api/isVerified?id=" + uuid.toString()).execute().returnResponse().getCode();
                if (responseCode == 200) {
                    RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
                    LuckPerms api = provider.getProvider();
                    User user = api.getPlayerAdapter(Player.class).getUser(player);
                    InheritanceNode node = InheritanceNode.builder(groupName).value(true).build();
                    user.data().add(node);
                    user.setPrimaryGroup(groupName);
                    api.getUserManager().saveUser(user);
                    player.sendMessage("You've successfully verified with World ID!");
                    this.cancel();
                } else {
                    player.sendMessage("Awaiting World ID verification. Retries left: " + counter);
                }
            } catch (Exception e) {
                player.sendMessage("Error while verifying with World ID: ", e.toString());
                this.cancel();
            } finally {
                counter--;
            }
        } else {
            player.sendMessage("Timed out waiting for verification. Please try again.");
            this.cancel();
        }
    }
}