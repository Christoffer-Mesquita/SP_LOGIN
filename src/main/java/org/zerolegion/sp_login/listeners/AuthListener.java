package org.zerolegion.sp_login.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.zerolegion.sp_login.SP_LOGIN;
import org.zerolegion.sp_login.manager.AuthManager;
import org.zerolegion.sp_login.utils.MessageUtils;
import org.zerolegion.sp_login.utils.TitleManager;

import java.util.HashMap;
import java.util.UUID;

public class AuthListener implements Listener {
    private final SP_LOGIN plugin;
    private final HashMap<UUID, Location> loginLocations;
    private final HashMap<UUID, BukkitRunnable> timeoutTasks;
    private final HashMap<UUID, BukkitRunnable> titleTasks;

    public AuthListener(SP_LOGIN plugin) {
        this.plugin = plugin;
        this.loginLocations = new HashMap<>();
        this.timeoutTasks = new HashMap<>();
        this.titleTasks = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // Remove a mensagem padrão de entrada
        event.setJoinMessage(null);
        
        // Primeiro teleporta o jogador para a área de autenticação
        plugin.getLocationManager().teleportToAuthLocation(player);
        
        // Depois carrega os dados do jogador
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // Carrega os dados do jogador
            plugin.getAuthManager().loadPlayer(player);
            
            // Verifica se tem sessão ativa
            if (plugin.getSessionManager().hasValidSession(player)) {
                plugin.getAuthManager().authenticate(uuid, "");
                player.sendMessage(MessageUtils.formatSuccess(
                    plugin.getConfig().getString("auth.messages.auto-login")));
                plugin.getLocationManager().teleportToSpawn(player);
                return;
            }
            
            // Envia mensagem de boas-vindas
            player.sendMessage("");
            player.sendMessage(MessageUtils.formatInfo(
                plugin.getConfig().getString("auth.messages.welcome")));
            
            // Aguarda um pouco para verificar o registro após o carregamento dos dados
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                // Configura mensagens iniciais baseado no status de registro
                if (plugin.getAuthManager().isRegistered(uuid)) {
                    player.sendMessage(MessageUtils.formatInfo(
                        plugin.getConfig().getString("auth.messages.login-required")));
                    startTitleTask(player, true);
                } else {
                    player.sendMessage(MessageUtils.formatInfo(
                        plugin.getConfig().getString("auth.messages.register-required")));
                    startTitleTask(player, false);
                }
                player.sendMessage("");

                // Inicia o timer de timeout
                startTimeoutTask(player);
            }, 10L); // Aguarda mais 10 ticks após carregar os dados
        }, 5L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Remove a mensagem padrão de saída
        event.setQuitMessage(null);
        
        UUID uuid = event.getPlayer().getUniqueId();
        cancelTimeoutTask(uuid);
        cancelTitleTask(uuid);
        loginLocations.remove(uuid);
        plugin.getAuthManager().removePlayer(uuid);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.ALLOWED) {
            UUID uuid = event.getPlayer().getUniqueId();
            cancelTitleTask(uuid);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAuthSuccess(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // Verifica se o jogador está autenticado
        if (plugin.getAuthManager().isAuthenticated(uuid)) {
            // Cancela as tarefas de título
            cancelTitleTask(uuid);
            
            // Cria a sessão
            plugin.getSessionManager().createSession(player);
            
            // Teleporta para o spawn
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.getLocationManager().teleportToSpawn(player);
                TitleManager.clearTitle(player);
            }, 5L);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getAuthManager().isAuthenticated(player.getUniqueId())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            
            // Permite apenas movimentação da cabeça
            if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
                event.setTo(from);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getAuthManager().isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.getAuthManager().isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!plugin.getAuthManager().isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.getAuthManager().isAuthenticated(event.getPlayer().getUniqueId())) {
            String command = event.getMessage().split(" ")[0].toLowerCase();
            if (!command.equals("/login") && !command.equals("/register")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.getAuthManager().isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!plugin.getAuthManager().isAuthenticated(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemPickup(PlayerPickupItemEvent event) {
        if (!plugin.getAuthManager().isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    private void startTitleTask(Player player, boolean isLogin) {
        UUID uuid = player.getUniqueId();
        cancelTitleTask(uuid);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline() && !plugin.getAuthManager().isAuthenticated(uuid)) {
                    if (isLogin) {
                        TitleManager.sendLoginTitle(player);
                    } else {
                        TitleManager.sendRegisterTitle(player);
                    }
                } else {
                    cancel();
                    if (player.isOnline()) {
                        TitleManager.clearTitle(player);
                    }
                }
            }
        };

        task.runTaskTimer(plugin, 2L, 100L); // Começa após 2 ticks e atualiza a cada 5 segundos
        titleTasks.put(uuid, task);
    }

    private void cancelTitleTask(UUID uuid) {
        BukkitRunnable task = titleTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    private void startTimeoutTask(Player player) {
        UUID uuid = player.getUniqueId();
        cancelTimeoutTask(uuid);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline() && !plugin.getAuthManager().isAuthenticated(uuid)) {
                    player.kickPlayer(MessageUtils.formatError(
                        plugin.getConfig().getString("auth.messages.timeout")));
                }
            }
        };

        task.runTaskLater(plugin, plugin.getConfig().getInt("auth.timeout", 60) * 20L);
        timeoutTasks.put(uuid, task);
    }

    private void cancelTimeoutTask(UUID uuid) {
        BukkitRunnable task = timeoutTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }
} 