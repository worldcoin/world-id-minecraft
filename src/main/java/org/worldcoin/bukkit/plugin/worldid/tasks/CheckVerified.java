package org.worldcoin.bukkit.plugin.worldid.tasks;

import java.util.UUID;

import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Response;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;
import org.worldcoin.bukkit.plugin.worldid.WorldId;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.model.user.User;

public class CheckVerified extends BukkitRunnable {


    private WorldId plugin = WorldId.getPlugin(WorldId.class);

    private final Player player;
    private final UUID uuid;
    private final String url;
    private int counter;

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
                Response response = Request.get(url).execute();
                int responseCode = response.returnResponse().getCode();
                String responseBody = response.returnContent().asString();
                if (responseCode == 200) {
                    String groupName;
                    switch(responseBody) {
                        case "orb":
                            groupName = plugin.orbGroupName;
                            break;
                        case "device":
                            groupName = plugin.deviceGroupName;
                            if (groupName == null) {
                                player.sendMessage("This Verification Level is not accepted.");
                                this.cancel();
                            }
                            break;
                        default:
                            throw new IllegalStateException("invalid response body");
                    }
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