package org.worldcoin.bukkit.plugin.worldid.tasks;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.worldcoin.bukkit.plugin.worldid.WorldId;

import com.posthog.java.PostHog;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.model.user.User;

public class CheckVerified extends BukkitRunnable {

    private WorldId plugin = WorldId.getPlugin(WorldId.class);

    private final Player player;
    private final String webUrl;
    private int counter;

    private FileConfiguration config = plugin.getConfig();
    private String orbGroupName = config.getString("orb-group-name");
    private String deviceGroupName = config.getString("device-group-name");
    private String baseUrl = config.getString("web-url");

    PostHog posthog = new PostHog.Builder(WorldId.POSTHOG_API_KEY).host(WorldId.POSTHOG_HOST).build();

    public CheckVerified(Player player, UUID uuid, int counter) {
        this.player = player;
        this.webUrl = baseUrl+"/api/isVerified?id="+uuid;
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
                Request.get(webUrl).execute().handleResponse(new HttpClientResponseHandler<Boolean>() {
                    @Override
                    public Boolean handleResponse(final ClassicHttpResponse response) throws IOException {
                        final int status = response.getCode();

                        if (status == 200) {
                            String verification_level = new String(response.getEntity().getContent().readAllBytes());
                            String groupName;
                            
                            switch (verification_level) {
                                case "orb":
                                    groupName = orbGroupName.isBlank() ? deviceGroupName : orbGroupName;
                                    break;
                                case "device":
                                    groupName = deviceGroupName;
                                    if (groupName == null) {
                                        player.sendMessage("This Verification Level is not accepted.");
                                        CheckVerified.this.cancel();
                                        return false;
                                    }
                                    break;
                                default:
                                    groupName = null;
                                    player.sendMessage("Invalid Verification Level.");
                                    CheckVerified.this.cancel();
                                    return false;
                            }

                            if (player.hasPermission("group." + groupName)) {
                                throw new IllegalStateException("player is already verified");
                            }

                            final LuckPerms api = LuckPermsProvider.get();
                            final User user = api.getPlayerAdapter(Player.class).getUser(player);
                            final InheritanceNode node = InheritanceNode.builder(groupName).value(true).build();
                            user.data().add(node);
                            user.setPrimaryGroup(groupName);
                            api.getUserManager().saveUser(user);
                            player.sendMessage("You've successfully verified with World ID!");
                            posthog.capture(player.getUniqueId().toString(), "minecraft integration verification", new HashMap<String, Object>() {
                                {
                                  put("verification_level", verification_level);
                                  put("server_uuid", plugin.getServer().getServerId());
                                }
                            });
                            CheckVerified.this.cancel();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
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