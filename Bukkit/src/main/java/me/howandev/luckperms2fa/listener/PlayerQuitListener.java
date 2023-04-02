package me.howandev.luckperms2fa.listener;

import me.howandev.luckperms2fa.LuckPerms2FA;
import me.howandev.luckperms2fa.auth.AuthManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        AuthManager authManager = LuckPerms2FA.getInstance().getAuthManager();
        authManager.removeUserFromVerified(player.getUniqueId());
        authManager.removeUserFromCache(player.getUniqueId());
    }
}
