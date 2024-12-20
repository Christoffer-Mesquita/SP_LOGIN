package org.zerolegion.sp_login.manager;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.zerolegion.sp_login.SP_LOGIN;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LocationManager {
    private final SP_LOGIN plugin;
    private Location spawnLocation;
    private final Map<UUID, Location> returnLocations;
    private static final int AUTH_HEIGHT = 1000; // Altura fixa para autenticação

    public LocationManager(SP_LOGIN plugin) {
        this.plugin = plugin;
        this.returnLocations = new HashMap<>();
        loadSpawnLocation();
    }

    private void loadSpawnLocation() {
        FileConfiguration config = plugin.getConfig();
        if (config.contains("locations.spawn")) {
            World world = plugin.getServer().getWorld(config.getString("locations.spawn.world"));
            if (world != null) {
                spawnLocation = new Location(
                    world,
                    config.getDouble("locations.spawn.x"),
                    config.getDouble("locations.spawn.y"),
                    config.getDouble("locations.spawn.z"),
                    (float) config.getDouble("locations.spawn.yaw"),
                    (float) config.getDouble("locations.spawn.pitch")
                );
            }
        }
    }

    public void setSpawnLocation(Location location) {
        this.spawnLocation = location.clone();
        
        FileConfiguration config = plugin.getConfig();
        config.set("locations.spawn.world", location.getWorld().getName());
        config.set("locations.spawn.x", location.getX());
        config.set("locations.spawn.y", location.getY());
        config.set("locations.spawn.z", location.getZ());
        config.set("locations.spawn.yaw", location.getYaw());
        config.set("locations.spawn.pitch", location.getPitch());
        
        plugin.saveConfig();
    }

    public void teleportToAuthLocation(Player player) {
        // Salva a localização atual para retornar depois
        if (!returnLocations.containsKey(player.getUniqueId())) {
            returnLocations.put(player.getUniqueId(), player.getLocation().clone());
        }

        // Cria uma localização no Y=1000
        Location authLocation = new Location(
            player.getWorld(),
            0.5, // Centro do bloco
            AUTH_HEIGHT,
            0.5, // Centro do bloco
            player.getLocation().getYaw(),
            0 // Pitch 0 para olhar reto
        );
        
        // Força o carregamento do chunk
        authLocation.getChunk().load(true);
        
        // Cria a plataforma e teleporta o jogador imediatamente
        createAuthPlatform(authLocation);
        player.teleport(authLocation);
    }

    public void teleportToSpawn(Player player) {
        if (spawnLocation != null) {
            Location previousLoc = player.getLocation();
            
            // Força o carregamento do chunk do spawn
            spawnLocation.getChunk().load(true);
            
            // Teleporta o jogador
            player.teleport(spawnLocation);
            returnLocations.remove(player.getUniqueId());
            
            // Remove a plataforma antiga
            removeAuthPlatform(new Location(previousLoc.getWorld(), 0.5, AUTH_HEIGHT - 1, 0.5));
        }
    }

    private void createAuthPlatform(Location location) {
        // Cria uma plataforma de vidro escuro
        location.subtract(0, 1, 0);
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                Location blockLoc = location.clone().add(x, 0, z);
                blockLoc.getBlock().setType(org.bukkit.Material.STAINED_GLASS);
                blockLoc.getBlock().setData((byte) 15); // Vidro preto
            }
        }
    }

    private void removeAuthPlatform(Location location) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                Location blockLoc = location.clone().add(x, 0, z);
                blockLoc.getBlock().setType(org.bukkit.Material.AIR);
            }
        }
    }

    public Location getReturnLocation(UUID uuid) {
        return returnLocations.get(uuid);
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }
} 