package me.howandev.luckperms2fa.listener;

import me.howandev.luckperms2fa.LuckPerms2FA;
import me.howandev.luckperms2fa.auth.AuthManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        AuthManager authManager = LuckPerms2FA.getInstance().getAuthManager();
        authManager.removeUserFromVerified(player.getUniqueId());
        if (!authManager.isContextBound(LuckPerms2FA.getInstance().getLuckPermsUser(player))) {
            return;
        }

        authManager.loadUserFromStorage(player.getUniqueId());
    }
}
