package me.howandev.luckperms2fa.command;

import me.howandev.luckperms2fa.LuckPerms2FA;
import me.howandev.luckperms2fa.auth.AuthManager;
import me.howandev.luckperms2fa.auth.AuthUser;
import me.howandev.luckperms2fa.settings.MfaSettings;
import me.howandev.luckperms2fa.totp.Authenticator;
import me.howandev.luckperms2fa.util.MessageParser;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;


//TODO: this is done so lazy... absolutely horrible code lies here.
public class VerifyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String commandLabel, @NotNull String[] args) {
        MessageParser parser = LuckPerms2FA.getInstance().getMessageParser();
        MfaSettings settings = LuckPerms2FA.getPluginSettings().getMfaSettings();
        if (sender instanceof ConsoleCommandSender console) {
            Player player = Bukkit.getPlayer(String.join(" ", args));
            if (player != null) {
                AuthManager authManager = LuckPerms2FA.getInstance().getAuthManager();
                Authenticator authenticator = LuckPerms2FA.getInstance().getAuthenticator();
                authManager.loadUser(player.getUniqueId()).thenAcceptAsync(authUser -> {
                    if (authUser == null || authUser.isCreationComplete()) {
                        console.sendMessage("That player does not have an authenticator.");
                        return;
                    }

                    authUser.setLastVerificationTime(authenticator.getTime());
                    authManager.addUserToCache(authUser);
                    authManager.addUserToVerified(authUser);
                    console.sendMessage("Forcefully verified '"+player.getName()+"'");
                });
            }

            console.sendMessage("There is no online player named '"+String.join(" ", args)+"'");
            return true;
        }

        if (sender instanceof Player player) {
            Map<String, String> placeholders = MessageParser.placeholdersFor(player);
            AuthManager authManager = LuckPerms2FA.getInstance().getAuthManager();
            if (!authManager.isContextBound(LuckPerms2FA.getInstance().getLuckPermsUser(player))) {
                return true;
            }

            Authenticator authenticator = LuckPerms2FA.getInstance().getAuthenticator();
            authManager.loadUser(player.getUniqueId()).thenAcceptAsync(authUser -> {
                if (authUser != null) {
                    //Cache user
                    authManager.addUserToCache(authUser);

                    boolean isValid = authenticator.isCodeValid(authUser.getSecret(), String.join(" ", args));
                    if (!authUser.isCreationComplete()) {
                        if (isValid) {
                            AuthUser createdUser = new AuthUser(authUser.getUniqueId(), authUser.getSecret(), true);
                            authManager.saveUserToStorage(createdUser);

                            createdUser.setLastVerificationTime(authenticator.getTime());
                            authManager.addUserToVerified(createdUser);
                            authManager.addUserToCache(createdUser);

                            player.spigot().sendMessage(parser.format("messages.success.auth-created", placeholders));
                            return;
                        }

                        player.spigot().sendMessage(parser.format("messages.failure.code-not-valid", placeholders));
                        return;
                    }

                    if (authManager.isAuthValid(authUser.getUniqueId(), settings.validationTimeout())) {
                        player.spigot().sendMessage(parser.format("messages.info.already-verified", placeholders));
                        return;
                    }

                    if (isValid) {
                        //Verify user
                        authUser.setLastVerificationTime(authenticator.getTime());
                        authManager.addUserToVerified(authUser);

                        player.spigot().sendMessage(parser.format("messages.success.verified", placeholders));
                        return;
                    }

                    player.spigot().sendMessage(parser.format("messages.failure.code-not-valid", placeholders));
                    return;
                }

                player.spigot().sendMessage(parser.format("messages.info.creating-user", placeholders));
                String secret = authenticator.generateSecret();
                AuthUser createdUser = new AuthUser(player.getUniqueId(), secret);
                authManager.addUserToCache(createdUser);
                try {
                    String label = parser.replacePlaceholders(settings.authLabel(), placeholders);
                    String issuer = parser.replacePlaceholders(settings.authIssuer(), placeholders);

                    placeholders.put("data", authenticator.generateQrData(label, secret, issuer));
                    player.spigot().sendMessage(parser.format("messages.info.qr-url", placeholders));
                } catch (Exception ex) {
                    player.spigot().sendMessage(parser.format("messages.failure.cannot-generate-qr", placeholders));
                }
            });

            return true;
        }

        return false;
    }
}
