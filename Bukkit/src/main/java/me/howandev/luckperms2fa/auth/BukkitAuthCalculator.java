package me.howandev.luckperms2fa.auth;

import me.howandev.luckperms2fa.LuckPerms2FA;
import me.howandev.luckperms2fa.context.AuthContextKeys;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BukkitAuthCalculator extends AuthCalculator<Player> {
    public BukkitAuthCalculator(AuthManager authManager) {
        super(authManager);
    }
    @Override
    public void calculate(@NotNull Player target, @NotNull ContextConsumer consumer) {
        User user = LuckPerms2FA.getInstance().getLuckPermsUser(target);
        if (authManager.isContextBound(user) && authManager.isAuthValid(target.getUniqueId(), LuckPerms2FA.getPluginSettings().getMfaSettings().validationTimeout())) {
            consumer.accept(AuthContextKeys.AUTH_KEY, "true");
            return;
        }

        consumer.accept(AuthContextKeys.AUTH_KEY, "false");
    }

    @Override
    public @NotNull ContextSet estimatePotentialContexts() {
        ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
        builder.add(AuthContextKeys.AUTH_KEY, "true");
        builder.add(AuthContextKeys.AUTH_KEY, "false");
        return builder.build();
    }
}
