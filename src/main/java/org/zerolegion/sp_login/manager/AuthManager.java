package org.zerolegion.sp_login.manager;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;
import org.zerolegion.sp_login.SP_LOGIN;
import org.zerolegion.sp_login.data.PlayerData;
import org.zerolegion.sp_login.utils.MessageUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthManager {
    private final SP_LOGIN plugin;
    private final MongoCollection<Document> playersCollection;
    private final Map<UUID, PlayerData> playerCache;

    public AuthManager(SP_LOGIN plugin) {
        this.plugin = plugin;
        this.playersCollection = plugin.getMongoDB().getCollection("players");
        this.playerCache = new HashMap<>();
    }

    public void loadPlayer(Player player) {
        try {
            // Primeiro cria os dados do jogador em cache com o IP atual
            PlayerData playerData = new PlayerData(player.getUniqueId(), player.getName());
            String ip = player.getAddress().getAddress().getHostAddress();
            playerData.setLastIp(ip);
            
            // Carrega os dados do MongoDB de forma síncrona inicialmente
            Document doc = playersCollection.find(Filters.eq("uuid", player.getUniqueId().toString())).first();
            if (doc != null) {
                playerData.setPasswordHash(doc.getString("passwordHash"));
                playerData.setLastLogin(doc.getLong("lastLogin"));
            }
            
            // Coloca no cache após carregar todos os dados
            playerCache.put(player.getUniqueId(), playerData);
            
        } catch (Exception e) {
            plugin.getLogger().severe("Erro crítico ao carregar jogador " + player.getName() + ": " + e.getMessage());
            // Mesmo com erro, cria um cache básico para evitar NPE
            playerCache.put(player.getUniqueId(), new PlayerData(player.getUniqueId(), player.getName()));
        }
    }

    public void savePlayer(PlayerData playerData) {
        Document doc = new Document("uuid", playerData.getUuid().toString())
                .append("username", playerData.getUsername())
                .append("passwordHash", playerData.getPasswordHash())
                .append("lastIp", playerData.getLastIp())
                .append("lastLogin", playerData.getLastLogin());

        playersCollection.replaceOne(
                Filters.eq("uuid", playerData.getUuid().toString()),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    public boolean isRegistered(UUID uuid) {
        try {
            PlayerData playerData = playerCache.get(uuid);
            if (playerData != null) {
                if (playerData.getPasswordHash() != null) {
                    return true;
                }
                // Se não tem hash no cache, verifica no banco
                Document doc = playersCollection.find(Filters.eq("uuid", uuid.toString())).first();
                if (doc != null && doc.getString("passwordHash") != null) {
                    // Atualiza o cache com o hash encontrado
                    playerData.setPasswordHash(doc.getString("passwordHash"));
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao verificar registro do jogador: " + e.getMessage());
            return false;
        }
    }

    public boolean isRegistered(String username) {
        try {
            // Primeiro tenta encontrar um jogador online
            Player player = Bukkit.getPlayer(username);
            if (player != null) {
                return isRegistered(player.getUniqueId());
            }
            
            // Se não estiver online, procura no banco de dados
            Document doc = playersCollection.find(Filters.eq("username", username)).first();
            return doc != null && doc.getString("passwordHash") != null;
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao verificar registro por nome: " + e.getMessage());
            return false;
        }
    }

    public boolean authenticate(UUID uuid, String password) {
        PlayerData playerData = playerCache.get(uuid);
        if (playerData == null || playerData.getPasswordHash() == null) {
            return false;
        }

        boolean authenticated = BCrypt.checkpw(password, playerData.getPasswordHash());
        if (authenticated) {
            playerData.setAuthenticated(true);
            playerData.setLastLogin(System.currentTimeMillis());
            
            // Atualiza o IP do jogador
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                String ip = player.getAddress().getAddress().getHostAddress();
                playerData.setLastIp(ip);
                // Salva os dados atualizados
                savePlayer(playerData);
            }
        }
        return authenticated;
    }

    public void register(UUID uuid, String password) {
        try {
            PlayerData playerData = playerCache.get(uuid);
            if (playerData != null) {
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                playerData.setPasswordHash(hashedPassword);
                playerData.setAuthenticated(true);
                playerData.setLastLogin(System.currentTimeMillis());
                
                // Salva o IP do jogador
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    String ip = player.getAddress().getAddress().getHostAddress();
                    playerData.setLastIp(ip);
                }
                
                // Salva os dados de forma assíncrona
                savePlayer(playerData);
                
                // Notifica o jogador imediatamente
                if (player != null && player.isOnline()) {
                    player.sendMessage(MessageUtils.formatSuccess(
                        plugin.getConfig().getString("auth.messages.register-success")));
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao registrar jogador: " + e.getMessage());
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(MessageUtils.formatError("&cErro ao registrar. Tente novamente em alguns segundos."));
            }
        }
    }

    public boolean isAuthenticated(UUID uuid) {
        PlayerData playerData = playerCache.get(uuid);
        return playerData != null && playerData.isAuthenticated();
    }

    public void removePlayer(UUID uuid) {
        playerCache.remove(uuid);
    }

    public void unregisterPlayer(String username) {
        // Primeiro tenta encontrar um jogador online
        Player player = Bukkit.getPlayer(username);
        if (player != null) {
            UUID uuid = player.getUniqueId();
            playerCache.remove(uuid);
            playersCollection.deleteOne(Filters.eq("uuid", uuid.toString()));
            return;
        }
        
        // Se não estiver online, remove do banco de dados
        playersCollection.deleteOne(Filters.eq("username", username));
    }

    public UUID getUUIDFromUsername(String username) {
        // Primeiro tenta encontrar um jogador online
        Player player = Bukkit.getPlayer(username);
        if (player != null) {
            return player.getUniqueId();
        }
        
        // Se não estiver online, procura no banco de dados
        Document doc = playersCollection.find(Filters.eq("username", username)).first();
        if (doc != null) {
            return UUID.fromString(doc.getString("uuid"));
        }
        return null;
    }
} 