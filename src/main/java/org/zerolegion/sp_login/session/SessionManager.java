package org.zerolegion.sp_login.session;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.zerolegion.sp_login.SP_LOGIN;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SessionManager {
    private final SP_LOGIN plugin;
    private final Map<String, SessionData> sessions;
    private final long sessionDuration;
    private final boolean enabled;
    private final MongoCollection<Document> sessionsCollection;

    public SessionManager(SP_LOGIN plugin) {
        this.plugin = plugin;
        this.sessions = new HashMap<>();
        this.sessionDuration = plugin.getConfig().getLong("session.duration", 1440) * 60 * 1000; // Converte minutos para milissegundos
        this.enabled = plugin.getConfig().getBoolean("session.enabled", true);
        this.sessionsCollection = plugin.getMongoDB().getCollection("sessions");
        
        // Carrega sessões existentes do MongoDB
        loadSessions();
    }

    private void loadSessions() {
        try {
            sessionsCollection.find().forEach(doc -> {
                String key = doc.getString("key");
                long creationTime = doc.getLong("creationTime");
                // Só carrega sessões que ainda não expiraram
                if (System.currentTimeMillis() - creationTime <= sessionDuration) {
                    sessions.put(key, new SessionData(creationTime));
                } else {
                    // Remove sessões expiradas do banco
                    sessionsCollection.deleteOne(Filters.eq("key", key));
                }
            });
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao carregar sessões: " + e.getMessage());
        }
    }

    public void createSession(Player player) {
        if (!enabled) return;
        String ip = player.getAddress().getAddress().getHostAddress();
        UUID uuid = player.getUniqueId();
        String key = getSessionKey(uuid, ip);
        long creationTime = System.currentTimeMillis();
        
        sessions.put(key, new SessionData(creationTime));
        
        // Salva a sessão no MongoDB
        try {
            Document sessionDoc = new Document("key", key)
                    .append("uuid", uuid.toString())
                    .append("ip", ip)
                    .append("creationTime", creationTime);
            
            sessionsCollection.replaceOne(
                    Filters.eq("key", key),
                    sessionDoc,
                    new ReplaceOptions().upsert(true)
            );
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao salvar sessão: " + e.getMessage());
        }
    }

    public boolean hasValidSession(Player player) {
        if (!enabled) return false;
        String ip = player.getAddress().getAddress().getHostAddress();
        UUID uuid = player.getUniqueId();
        String key = getSessionKey(uuid, ip);
        
        // Primeiro verifica no cache
        SessionData session = sessions.get(key);
        if (session == null) {
            // Se não está no cache, verifica no MongoDB
            try {
                Document doc = sessionsCollection.find(Filters.eq("key", key)).first();
                if (doc != null) {
                    long creationTime = doc.getLong("creationTime");
                    session = new SessionData(creationTime);
                    sessions.put(key, session);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Erro ao verificar sessão: " + e.getMessage());
                return false;
            }
        }
        
        if (session == null) return false;

        if (System.currentTimeMillis() - session.getCreationTime() > sessionDuration) {
            sessions.remove(key);
            try {
                sessionsCollection.deleteOne(Filters.eq("key", key));
            } catch (Exception e) {
                plugin.getLogger().warning("Erro ao remover sessão expirada: " + e.getMessage());
            }
            return false;
        }

        return true;
    }

    public String getTimeRemaining(Player player) {
        if (!enabled) return "Sistema de sessão desativado";
        
        String ip = player.getAddress().getAddress().getHostAddress();
        UUID uuid = player.getUniqueId();
        String key = getSessionKey(uuid, ip);
        
        SessionData session = sessions.get(key);
        if (session == null) {
            // Verifica no MongoDB se não está no cache
            try {
                Document doc = sessionsCollection.find(Filters.eq("key", key)).first();
                if (doc != null) {
                    session = new SessionData(doc.getLong("creationTime"));
                    sessions.put(key, session);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Erro ao verificar tempo de sessão: " + e.getMessage());
            }
        }
        
        if (session == null) {
            return "Sem sessão ativa";
        }

        long elapsedTime = System.currentTimeMillis() - session.getCreationTime();
        long remainingTime = sessionDuration - elapsedTime;

        if (remainingTime <= 0) {
            sessions.remove(key);
            try {
                sessionsCollection.deleteOne(Filters.eq("key", key));
            } catch (Exception e) {
                plugin.getLogger().warning("Erro ao remover sessão expirada: " + e.getMessage());
            }
            return "Sessão expirada";
        }

        return formatDuration(remainingTime);
    }

    public void clearSession(Player player) {
        if (!enabled) return;
        String ip = player.getAddress().getAddress().getHostAddress();
        UUID uuid = player.getUniqueId();
        String key = getSessionKey(uuid, ip);
        
        sessions.remove(key);
        try {
            sessionsCollection.deleteOne(Filters.eq("key", key));
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao limpar sessão: " + e.getMessage());
        }
    }

    private String getSessionKey(UUID uuid, String ip) {
        return uuid.toString() + ":" + ip;
    }

    private String formatDuration(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        
        StringBuilder time = new StringBuilder();
        if (hours > 0) {
            time.append(hours).append("h ");
        }
        if (minutes > 0 || hours == 0) {
            time.append(minutes).append("m");
        }
        
        return time.toString().trim();
    }

    private static class SessionData {
        private final long creationTime;

        public SessionData(long creationTime) {
            this.creationTime = creationTime;
        }

        public long getCreationTime() {
            return creationTime;
        }
    }
} 