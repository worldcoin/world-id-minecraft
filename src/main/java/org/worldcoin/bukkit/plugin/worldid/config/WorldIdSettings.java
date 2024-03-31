package org.worldcoin.bukkit.plugin.worldid.config;

import com.posthog.java.PostHog;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.worldcoin.bukkit.plugin.worldid.WorldIdPlugin;

public class WorldIdSettings {

    private static final String POSTHOG_API_KEY = "phc_QttqgDbMQDYHX1EMH7FnT6ECBVzdp0kGUq92aQaVQ6I";
    private static final String POSTHOG_HOST = "https://app.posthog.com";

    private final PostHog posthog;

    private final WorldIdPlugin plugin;

    private final String orbGroupName;
    private final String deviceGroupName;
    private String uuid;
    private final String webUrl;

    public WorldIdSettings(WorldIdPlugin plugin) {
        plugin.saveDefaultConfig();
        final ConfigurationSection config = plugin.getConfig();

        this.plugin = plugin;
        this.orbGroupName = config.getString("orb-group-name", "");
        this.deviceGroupName = config.getString("device-group-name", "");
        this.uuid = config.getString("server-uuid", "");
        this.webUrl = config.getString("web-url", "");
        this.posthog = new PostHog.Builder(POSTHOG_API_KEY).host(POSTHOG_HOST).build();
    }

    public boolean isValid() {
        final Logger logger = this.plugin.getLogger();

        if (this.uuid.isEmpty()) {
            logger.warning("You must configure a server UUID in config.yml file before using this plugin!");
            return false;
        }
        if (this.orbGroupName.isEmpty() && this.deviceGroupName.isEmpty()) {
            logger.warning("You have not configured an Orb Group Name or Device Group Name in config.yml. At least one group must be configured.");
            return false;
        }
        if (this.webUrl.isEmpty()) {
            logger.warning("You have not configured a Web URL in config.yml. This is https://minecraft.worldcoin.org by default. This must be configured.");
            return false;
        }

        if (this.orbGroupName.isEmpty()) {
            logger.info("You have not configured an Orb Group Name in config.yml. All World ID-verified users will be issued the same role regardless of their verification level.");
            // Fall-through
        }
        if (this.deviceGroupName.isEmpty()) {
            logger.info("You have not configured a Device Group Name in config.yml. Only Orb-verified World ID users will be able to verify.");
            // Fall-through
        }

        return true;
    }

    public String getDeviceGroupName() {
        return this.deviceGroupName;
    }

    public String getOrbGroupName() {
        return this.orbGroupName;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getWebUrl() {
        return this.webUrl;
    }

    public PostHog getPosthog() {
        return this.posthog;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
        this.plugin.getConfig().set("server-uuid", uuid);
    }

    public void save() {
        this.plugin.saveConfig();
    }
}
