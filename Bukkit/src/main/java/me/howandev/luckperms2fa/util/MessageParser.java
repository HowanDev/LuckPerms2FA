package me.howandev.luckperms2fa.util;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MessageParser {
    private final @NotNull ConfigurationSection section;
    public MessageParser(@NotNull ConfigurationSection configuration) {
        this.section = configuration;
    }

    public String replacePlaceholders(String message, @Nullable Map<String, String> placeholders) {
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replaceAll("%"+entry.getKey()+"%", entry.getValue());
            }
        }

        return message;
    }

    public BaseComponent[] formatJson(String json) {
        return ComponentSerializer.parse(json);
    }

    public BaseComponent[] formatLegacy(String legacyText) {
        return new ComponentBuilder().appendLegacy(legacyText).create();
    }

    public BaseComponent[] format(@NotNull String key, @Nullable Map<String, String> placeholders) {
        String legacyOrJson = section.getString(key, "MissingNo");
        assert legacyOrJson != null;

        legacyOrJson = replacePlaceholders(legacyOrJson, placeholders);

        try {
            BaseComponent[] jsonComponents = formatJson(legacyOrJson);
            if (jsonComponents.length >= 1) return jsonComponents;
        } catch (Exception ignored) { }


        return formatLegacy(legacyOrJson);
    }

    public static Map<String, String> placeholdersFor(@NotNull Player player) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player_name", player.getName());

        return placeholders;
    }
}
