package me.howandev.luckperms2fa;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.md_5.bungee.api.plugin.Plugin;

public class LuckPerms2FA extends Plugin {
    private static LuckPerms2FA instance;
    private LuckPerms luckPermsApi;
    @Override
    public void onEnable() {
        instance = this;
        luckPermsApi = LuckPermsProvider.get();
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public static LuckPerms2FA getInstance() {
        return instance;
    }
}
