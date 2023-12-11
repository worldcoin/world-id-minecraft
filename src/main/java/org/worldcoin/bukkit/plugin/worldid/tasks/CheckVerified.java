package org.worldcoin.bukkit.plugin.worldid.tasks;

import java.io.IOException;
import java.util.UUID;

import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;
import org.worldcoin.bukkit.plugin.worldid.WorldId;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.model.user.User;

public class CheckVerified extends BukkitRunnable {

    private final Player player;
    private final UUID uuid;
    private final String url;
    private int counter;

    private WorldId plugin = WorldId.getPlugin(WorldId.class);

    public CheckVerified(Player player, UUID uuid, String url, int counter) {
        this.player = player;
        this.uuid = uuid;
        this.url = url;
        if (counter <= 0) {
            throw new IllegalArgumentException("counter must be greater than 0");
        } else {
            this.counter = counter;
        }
    }

    @Override
    public void run() {
        if (counter > 0) { 
            try {
                Boolean success = Request.get(url + "/api/isVerified?id=" + uuid.toString()).execute().handleResponse(new HttpClientResponseHandler<Boolean>() {
                    public Boolean handleResponse(final ClassicHttpResponse response) throws IOException {
                        int code = response.getCode();
                        String group = response.getEntity().toString();
                        if (code == 200) {
                            String groupName = plugin.getConfig().getString("world-id-" + group + "-group-name");
                            if (player.hasPermission("group." + groupName)) {
                                throw new IllegalStateException("player is already verified");
                            }
                            RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
                            LuckPerms api = provider.getProvider();
                            User user = api.getPlayerAdapter(Player.class).getUser(player);
                            InheritanceNode node = InheritanceNode.builder(groupName).value(true).build();
                            user.data().add(node);
                            user.setPrimaryGroup(groupName);
                            api.getUserManager().saveUser(user);
                            player.sendMessage("You've successfully verified with World ID!");
                            return true;
                        } else {
                            player.sendMessage("Awaiting World ID verification. Retries left: " + counter);
                            return false;
                        }
                    }
                });
                if (success) {
                    this.cancel();
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