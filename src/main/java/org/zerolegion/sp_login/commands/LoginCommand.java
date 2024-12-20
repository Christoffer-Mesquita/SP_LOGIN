package org.zerolegion.sp_login.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zerolegion.sp_login.SP_LOGIN;
import org.zerolegion.sp_login.manager.AuthManager;

import java.util.HashMap;
import java.util.UUID;

public class LoginCommand implements CommandExecutor {
    private final SP_LOGIN plugin;
    private final HashMap<UUID, Integer> loginAttempts;
    private final HashMap<UUID, Long> lastLoginAttempt;

    public LoginCommand(SP_LOGIN plugin) {
        this.plugin = plugin;
        this.loginAttempts = new HashMap<>();
        this.lastLoginAttempt = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Este comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        AuthManager authManager = plugin.getAuthManager();

        // Verifica se o jogador já está autenticado
        if (authManager.isAuthenticated(uuid)) {
            player.sendMessage(ChatColor.RED + "Você já está autenticado!");
            return true;
        }

        // Verifica se o jogador está registrado
        if (!authManager.isRegistered(uuid)) {
            player.sendMessage(ChatColor.RED + "Você precisa se registrar primeiro! Use /register <senha> <senha>");
            return true;
        }

        // Verifica proteção contra força bruta
        if (isRateLimited(uuid)) {
            player.sendMessage(ChatColor.RED + "Aguarde alguns segundos antes de tentar novamente!");
            return true;
        }

        // Verifica se a senha foi fornecida
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Use: /login <senha>");
            return true;
        }

        String password = args[0];

        // Tenta autenticar o jogador
        if (authManager.authenticate(uuid, password)) {
            player.sendMessage(ChatColor.GREEN + "Login realizado com sucesso!");
            loginAttempts.remove(uuid);
            lastLoginAttempt.remove(uuid);
            
            // Teleporta para o spawn após autenticação
            plugin.getLocationManager().teleportToSpawn(player);
        } else {
            // Incrementa tentativas de login
            int attempts = loginAttempts.getOrDefault(uuid, 0) + 1;
            loginAttempts.put(uuid, attempts);
            lastLoginAttempt.put(uuid, System.currentTimeMillis());

            if (attempts >= 5) {
                player.kickPlayer(ChatColor.RED + "Muitas tentativas de login incorretas!");
                loginAttempts.remove(uuid);
                lastLoginAttempt.remove(uuid);
            } else {
                player.sendMessage(ChatColor.RED + "Senha incorreta! Tentativas restantes: " + (5 - attempts));
            }
        }

        return true;
    }

    private boolean isRateLimited(UUID uuid) {
        Long lastAttempt = lastLoginAttempt.get(uuid);
        if (lastAttempt == null) return false;

        // Limita tentativas a cada 3 segundos
        return (System.currentTimeMillis() - lastAttempt) < 3000;
    }
} 