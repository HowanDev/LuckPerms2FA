package me.howandev.luckperms2fa.settings;

import org.bukkit.configuration.file.FileConfiguration;

public class PluginSettings {
    private FileConfiguration configuration;
    public PluginSettings(FileConfiguration configuration) {
        this.configuration = configuration;
    }

    public void reloadConfiguration(FileConfiguration configuration) {
        this.configuration = configuration;
    }

    public MfaSettings getMfaSettings() {
        return new MfaSettings(configuration.getConfigurationSection("mfa-settings"));
    }
}
