package me.howandev.luckperms2fa;

import me.howandev.luckperms2fa.auth.AuthCalculator;
import me.howandev.luckperms2fa.auth.AuthManager;
import me.howandev.luckperms2fa.auth.BukkitAuthCalculator;
import me.howandev.luckperms2fa.command.VerifyCommand;
import me.howandev.luckperms2fa.listener.PlayerJoinListener;
import me.howandev.luckperms2fa.listener.PlayerQuitListener;
import me.howandev.luckperms2fa.settings.PluginSettings;
import me.howandev.luckperms2fa.storage.sql.SqlStorage;
import me.howandev.luckperms2fa.totp.Authenticator;
import me.howandev.luckperms2fa.totp.authenticator.GoogleAuth;
import me.howandev.luckperms2fa.util.MessageParser;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

//TODO: rename to LuckPermsMFA, since its not really 2-factor authentication
public class LuckPerms2FA extends JavaPlugin {
    private static LuckPerms2FA instance;
    private static PluginSettings pluginSettings;

    private MessageParser messageParser;
    private Authenticator authenticator;
    private AuthManager authManager;
    private AuthCalculator<Player> authCalculator;
    private LuckPerms luckPermsApi;
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        pluginSettings = new PluginSettings(getConfig());
        messageParser = new MessageParser(getConfig());

        authenticator = new GoogleAuth(pluginSettings.getMfaSettings().hashingAlgorithm(), pluginSettings.getMfaSettings().codeLength(), pluginSettings.getMfaSettings().codeExpiry());
        authManager = new AuthManager(authenticator, new SqlStorage(new File(getDataFolder(), "userdata.db")));
        try {
            authManager.initStorage();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        authCalculator = new BukkitAuthCalculator(authManager);

        luckPermsApi = LuckPermsProvider.get();
        luckPermsApi.getContextManager().registerCalculator(authCalculator);

        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable() {
        try {
            authManager.shutdownStorage();
        } catch (Exception ex) {
            getLogger().severe("Failed to shutdown storage properly (This **MAY** cause some data loss!)");
            ex.printStackTrace();
        }

        instance = null;
    }

    public static LuckPerms2FA getInstance() {
        return instance;
    }

    public static PluginSettings getPluginSettings() {
        return pluginSettings;
    }

    public MessageParser getMessageParser() {
        return messageParser;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public AuthManager getAuthManager() {
        return authManager;
    }

    public AuthCalculator<Player> getAuthCalculator() {
        return authCalculator;
    }

    public LuckPerms getLuckPermsApi() {
        return luckPermsApi;
    }

    public User getLuckPermsUser(Player player) {
        return getLuckPermsApi().getPlayerAdapter(Player.class).getUser(player);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
    }

    private void registerCommands() {
        getCommand("verify").setExecutor(new VerifyCommand());
    }
}
