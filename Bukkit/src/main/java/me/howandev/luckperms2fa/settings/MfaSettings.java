package me.howandev.luckperms2fa.settings;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class MfaSettings {
    private final ConfigurationSection section;
    public MfaSettings(ConfigurationSection section) {
        this.section = section;
    }

    public int validationTimeout() {
        return section.getInt("validation-timeout", 300);
    }

    public @NotNull String authLabel() {
        String authLabel = section.getString("auth-label", "%player_name%");
        assert authLabel != null;

        return authLabel;
    }

    public @NotNull String authIssuer() {
        String authIssuer = section.getString("auth-issuer", "LuckPerms");
        assert authIssuer != null;

        return authIssuer;
    }

    public int tokenLength() {
        return section.getInt("token-length", 32);
    }

    public int codeLength() {
        return section.getInt("code-length", 6);
    }

    public @NotNull String hashingAlgorithm() {
        String hashingAlgorithm = section.getString("hashing-algorithm", "SHA1");
        assert hashingAlgorithm != null; //Stop screaming about non-existent nullability.

        return hashingAlgorithm;
    }

    public int codeExpiry() {
        return section.getInt("code-expiry", 30);
    }

    public @NotNull String qrUrl() {
        String url = section.getString("qr-url", "https://chart.googleapis.com/chart?&cht=qr&chs=500x500&chl=%data%");
        assert url != null;

        return url;
    }
}