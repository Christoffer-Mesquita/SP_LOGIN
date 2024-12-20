package org.zerolegion.sp_login.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zerolegion.sp_login.SP_LOGIN;
import org.zerolegion.sp_login.utils.MessageUtils;

public class SessionCommand implements CommandExecutor {
    private final SP_LOGIN plugin;

    public SessionCommand(SP_LOGIN plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.formatError("Este comando só pode ser usado por jogadores!"));
            return true;
        }

        Player player = (Player) sender;
        String timeRemaining = plugin.getSessionManager().getTimeRemaining(player);
        String message;

        if (timeRemaining.equals("Sem sessão ativa")) {
            message = MessageUtils.formatError(plugin.getConfig().getString("session.messages.not-found"));
        } else if (timeRemaining.equals("Sessão expirada")) {
            message = MessageUtils.formatWarning(plugin.getConfig().getString("session.messages.expired"));
        } else if (timeRemaining.equals("Sistema de sessão desativado")) {
            message = MessageUtils.formatError("O sistema de sessão está desativado.");
        } else {
            message = MessageUtils.formatSessionInfo(
                plugin.getConfig().getString("session.messages.active")
                    .replace("%time%", timeRemaining)
            );
        }

        player.sendMessage("");
        player.sendMessage(message);
        player.sendMessage("");
        return true;
    }
} 