package org.worldcoin.bukkit.plugin.worldid.tasks;

import com.posthog.java.PostHog;
import java.util.Map;
import java.util.UUID;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.apache.hc.client5.http.fluent.Request;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.worldcoin.bukkit.plugin.worldid.WorldIdPlugin;
import org.worldcoin.bukkit.plugin.worldid.config.WorldIdSettings;

public class CheckVerified extends BukkitRunnable {

    private final UUID playerId;
    private final String webUrl;
    private int attempts;

    private final WorldIdPlugin plugin;
    private final String orbGroupName;
    private final String deviceGroupName;
    private final String serverUUID;
    private final PostHog posthog;


    public CheckVerified(WorldIdPlugin plugin, Player player, UUID uuid, int attempts) {
        this.playerId = player.getUniqueId();
        this.plugin = plugin;

        final WorldIdSettings settings = plugin.getSettings();

        this.webUrl = settings.getWebUrl() + "/api/isVerified?id=" + uuid;
        this.orbGroupName = settings.getOrbGroupName();
        this.deviceGroupName = settings.getDeviceGroupName();
        this.posthog = settings.getPosthog();
        this.serverUUID = settings.getUuid();

        if (attempts <= 0) {
            throw new IllegalArgumentException("Attempts must be greater than 0");
        } else {
            this.attempts = attempts;
        }
    }

    @Override
    public void run() {
        final Player player = Bukkit.getPlayer(playerId);

        if (player == null) { // Player went offline
            this.cancel();
            return;
        }

        if (this.attempts <= 0) {
            player.sendMessage("Timed out waiting for verification. Please try again.");
            this.cancel();
            return;
        }

        try {
            Request.get(this.webUrl).execute().handleResponse(response -> {
                final int status = response.getCode();

                if (status != 200) {
                    return false;
                }

                String verificationLevel = new String(response.getEntity().getContent().readAllBytes());
                String expectedGroupName = getExpectedGroupName(verificationLevel);

                if (expectedGroupName == null) {
                    final String errorMessage = verificationLevel.equalsIgnoreCase("device") ?
                        "This Verification Level is not accepted." :
                        "Invalid Verification Level.";

                    player.sendMessage(errorMessage);
                    this.cancel();
                    return false;
                }

                if (this.plugin.isVerified(player)) {
                    throw new IllegalStateException("player is already verified");
                }

                this.finalizeVerification(player, expectedGroupName, verificationLevel);
                return true;
            });
        } catch (Exception exception) {
            player.sendMessage("Error while verifying with World ID: ", exception.toString());
            this.cancel();
        } finally {
            this.attempts--;
        }
    }

    private void finalizeVerification(Player player, String groupName, String verificationLevel) {
        final LuckPerms api = LuckPermsProvider.get();
        final User user = api.getPlayerAdapter(Player.class).getUser(player);
        final InheritanceNode node = InheritanceNode.builder(groupName).value(true).build();
        user.data().add(node);
        user.setPrimaryGroup(groupName);
        api.getUserManager().saveUser(user);

        player.sendMessage("You've successfully verified with World ID!");

        posthog.capture(player.getUniqueId().toString(), "minecraft integration verification", Map.of(
            "verificationLevel", verificationLevel,
            "server_uuid", serverUUID
        ));

        // Potential improvement: Call a custom event announcing verification is complete

        this.cancel();
    }

    private String getExpectedGroupName(String verificationLevel) {
        return switch (verificationLevel) {
            case "orb" -> orbGroupName.isBlank() ? deviceGroupName : orbGroupName;
            case "device" -> deviceGroupName;
            default -> null;
        };
    }
}