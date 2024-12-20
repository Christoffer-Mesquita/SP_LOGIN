package org.zerolegion.sp_login.data;

import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private final String username;
    private String passwordHash;
    private String lastIp;
    private long lastLogin;
    private boolean isAuthenticated;

    public PlayerData(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.isAuthenticated = false;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getLastIp() {
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }
} 