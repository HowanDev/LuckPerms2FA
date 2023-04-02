package me.howandev.luckperms2fa.auth;

import java.util.UUID;

//TODO: I already can see how this gets confused with LuckPerms AuthUser & LuckPerms2FA AuthUser
public record AuthUser(UUID uniqueId, String token) {
}
