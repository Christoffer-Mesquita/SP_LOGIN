package org.zerolegion.sp_login.manager;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.zerolegion.sp_login.SP_LOGIN;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogManager {
    private final SP_LOGIN plugin;
    private final MongoCollection<Document> logsCollection;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public LogManager(SP_LOGIN plugin) {
        this.plugin = plugin;
        this.logsCollection = plugin.getMongoDB().getCollection("auth_logs");
    }

    public void logLogin(Player player, boolean success, String ip) {
        Document log = new Document("type", "LOGIN")
                .append("player", player.getName())
                .append("uuid", player.getUniqueId().toString())
                .append("success", success)
                .append("ip", ip)
                .append("timestamp", System.currentTimeMillis())
                .append("date", DATE_FORMAT.format(new Date()));
        logsCollection.insertOne(log);
    }

    public void logRegister(Player player, String ip) {
        Document log = new Document("type", "REGISTER")
                .append("player", player.getName())
                .append("uuid", player.getUniqueId().toString())
                .append("ip", ip)
                .append("timestamp", System.currentTimeMillis())
                .append("date", DATE_FORMAT.format(new Date()));
        logsCollection.insertOne(log);
    }

    public void logFailedAttempt(Player player, String ip, String reason) {
        Document log = new Document("type", "FAILED_ATTEMPT")
                .append("player", player.getName())
                .append("uuid", player.getUniqueId().toString())
                .append("ip", ip)
                .append("reason", reason)
                .append("timestamp", System.currentTimeMillis())
                .append("date", DATE_FORMAT.format(new Date()));
        logsCollection.insertOne(log);
    }

    public void logAdminAction(Player admin, String target, String action) {
        Document log = new Document("type", "ADMIN_ACTION")
                .append("admin", admin.getName())
                .append("admin_uuid", admin.getUniqueId().toString())
                .append("target", target)
                .append("action", action)
                .append("timestamp", System.currentTimeMillis())
                .append("date", DATE_FORMAT.format(new Date()));
        logsCollection.insertOne(log);
    }

    public List<Document> getPlayerLogs(String playerName, int limit) {
        List<Document> logs = new ArrayList<>();
        logsCollection.find(Filters.eq("player", playerName))
                .sort(new Document("timestamp", -1))
                .limit(limit)
                .into(logs);
        return logs;
    }

    public List<Document> getRecentLogs(int limit) {
        List<Document> logs = new ArrayList<>();
        logsCollection.find()
                .sort(new Document("timestamp", -1))
                .limit(limit)
                .into(logs);
        return logs;
    }

    public List<Document> getFailedAttempts(String playerName, int limit) {
        List<Document> logs = new ArrayList<>();
        logsCollection.find(
                Filters.and(
                    Filters.eq("player", playerName),
                    Filters.eq("type", "FAILED_ATTEMPT")
                ))
                .sort(new Document("timestamp", -1))
                .limit(limit)
                .into(logs);
        return logs;
    }
} 