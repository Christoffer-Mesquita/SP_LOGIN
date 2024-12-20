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

public class RegisterCommand implements CommandExecutor {
    private final SP_LOGIN plugin;
    private final HashMap<UUID, Long> lastRegisterAttempt;

    public RegisterCommand(SP_LOGIN plugin) {
        this.plugin = plugin;
        this.lastRegisterAttempt = new HashMap<>();
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

        // Verifica se o jogador já está registrado
        if (authManager.isRegistered(uuid)) {
            player.sendMessage(ChatColor.RED + "Você já está registrado! Use /login <senha>");
            return true;
        }

        // Verifica proteção contra spam
        if (isRateLimited(uuid)) {
            player.sendMessage(ChatColor.RED + "Aguarde alguns segundos antes de tentar novamente!");
            return true;
        }

        // Verifica se as senhas foram fornecidas corretamente
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Use: /register <senha> <senha>");
            return true;
        }

        String password = args[0];
        String confirmPassword = args[1];

        // Verifica se as senhas coincidem
        if (!password.equals(confirmPassword)) {
            player.sendMessage(ChatColor.RED + "As senhas não coincidem!");
            return true;
        }

        // Verifica requisitos mínimos da senha
        if (password.length() < 6) {
            player.sendMessage(ChatColor.RED + "A senha deve ter pelo menos 6 caracteres!");
            return true;
        }

        // Registra o jogador
        authManager.register(uuid, password);
        player.sendMessage(ChatColor.GREEN + "Registro realizado com sucesso! Use /login <senha> para entrar.");

        // Teleporta para o spawn após registro
        plugin.getLocationManager().teleportToSpawn(player);

        lastRegisterAttempt.remove(uuid);
        return true;
    }

    private boolean isRateLimited(UUID uuid) {
        Long lastAttempt = lastRegisterAttempt.get(uuid);
        if (lastAttempt == null) return false;

        // Limita tentativas a cada 5 segundos
        return (System.currentTimeMillis() - lastAttempt) < 5000;
    }
} 