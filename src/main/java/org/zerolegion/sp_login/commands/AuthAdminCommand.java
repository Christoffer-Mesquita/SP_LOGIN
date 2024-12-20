package org.zerolegion.sp_login.commands;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zerolegion.sp_login.SP_LOGIN;
import org.zerolegion.sp_login.utils.MessageUtils;

import java.util.List;
import java.util.UUID;

public class AuthAdminCommand implements CommandExecutor {
    private final SP_LOGIN plugin;

    public AuthAdminCommand(SP_LOGIN plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("sensitive.auth.admin")) {
            sender.sendMessage(MessageUtils.formatError("&6Sensitive &f| &cVocê não tem permissão para usar este comando!"));
            return true;
        }

        // Se não houver argumentos ou apenas "admin", mostra ajuda
        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("admin"))) {
            sendHelp(sender);
            return true;
        }

        // Pega o subcomando (removendo o "admin" se presente)
        String subCommand = args.length > 1 && args[0].equalsIgnoreCase("admin") ? args[1].toLowerCase() : args[0].toLowerCase();
        
        switch (subCommand) {
            case "setspawn":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtils.formatError("&6Sensitive &f| &cEste comando só pode ser usado por jogadores!"));
                    return true;
                }
                setSpawn((Player) sender);
                break;

            case "spawn":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtils.formatError("&6Sensitive &f| &cEste comando só pode ser usado por jogadores!"));
                    return true;
                }
                teleportToSpawn((Player) sender);
                break;

            case "info":
                if (args.length < (args[0].equalsIgnoreCase("admin") ? 3 : 2)) {
                    sender.sendMessage(MessageUtils.formatError("&6Sensitive &f| &cUse: /auth admin info <jogador>"));
                    return true;
                }
                showPlayerInfo(sender, args[args[0].equalsIgnoreCase("admin") ? 2 : 1]);
                break;

            case "logs":
                if (args.length < (args[0].equalsIgnoreCase("admin") ? 3 : 2)) {
                    sender.sendMessage(MessageUtils.formatError("&6Sensitive &f| &cUse: /auth admin logs <jogador>"));
                    return true;
                }
                showPlayerLogs(sender, args[args[0].equalsIgnoreCase("admin") ? 2 : 1]);
                break;

            case "unregister":
                if (args.length < (args[0].equalsIgnoreCase("admin") ? 3 : 2)) {
                    sender.sendMessage(MessageUtils.formatError("&6Sensitive &f| &cUse: /auth admin unregister <jogador>"));
                    return true;
                }
                unregisterPlayer(sender, args[args[0].equalsIgnoreCase("admin") ? 2 : 1]);
                break;

            case "forcelogout":
                if (args.length < (args[0].equalsIgnoreCase("admin") ? 3 : 2)) {
                    sender.sendMessage(MessageUtils.formatError("&6Sensitive &f| &cUse: /auth admin forcelogout <jogador>"));
                    return true;
                }
                forceLogout(sender, args[args[0].equalsIgnoreCase("admin") ? 2 : 1]);
                break;

            case "recent":
                showRecentLogs(sender);
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void setSpawn(Player player) {
        plugin.getLocationManager().setSpawnLocation(player.getLocation());
        player.sendMessage(MessageUtils.formatSuccess(
            plugin.getConfig().getString("auth.messages.spawn-set")));
    }

    private void teleportToSpawn(Player player) {
        plugin.getLocationManager().teleportToSpawn(player);
        player.sendMessage(MessageUtils.formatSuccess(
            plugin.getConfig().getString("auth.messages.teleported-spawn")));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(MessageUtils.formatInfo("&6&lSensitive &8- &3&lPainel Administrativo"));
        sender.sendMessage("");
        sender.sendMessage(MessageUtils.formatInfo("&eComandos disponíveis:"));
        sender.sendMessage(MessageUtils.formatInfo("&8» &f/auth admin info <jogador> &7- &eVer informações de um jogador"));
        sender.sendMessage(MessageUtils.formatInfo("&8» &f/auth admin logs <jogador> &7- &eVer logs de um jogador"));
        sender.sendMessage(MessageUtils.formatInfo("&8» &f/auth admin unregister <jogador> &7- &eRemover registro de um jogador"));
        sender.sendMessage(MessageUtils.formatInfo("&8» &f/auth admin forcelogout <jogador> &7- &eForçar logout de um jogador"));
        sender.sendMessage(MessageUtils.formatInfo("&8» &f/auth admin recent &7- &eVer logs recentes"));
        sender.sendMessage(MessageUtils.formatInfo("&8» &f/auth admin setspawn &7- &eDefinir o spawn do servidor"));
        sender.sendMessage(MessageUtils.formatInfo("&8» &f/auth admin spawn &7- &eTeleportar para o spawn"));
        sender.sendMessage("");
    }

    private void showPlayerInfo(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        boolean isOnline = target != null && target.isOnline();
        boolean isRegistered = plugin.getAuthManager().isRegistered(playerName);
        UUID uuid = isOnline ? target.getUniqueId() : plugin.getAuthManager().getUUIDFromUsername(playerName);
        
        sender.sendMessage("");
        sender.sendMessage(MessageUtils.formatInfo("&6&lSensitive &8- &3&lInformações do Jogador"));
        sender.sendMessage("");
        sender.sendMessage(MessageUtils.formatInfo("&eJogador: &f" + playerName));
        sender.sendMessage(MessageUtils.formatInfo("&eStatus: " + (isOnline ? "&aOnline" : "&cOffline")));
        sender.sendMessage(MessageUtils.formatInfo("&eRegistrado: " + (isRegistered ? "&aSim" : "&cNão")));
        
        if (isOnline && uuid != null) {
            sender.sendMessage(MessageUtils.formatInfo("&eIP: &f" + target.getAddress().getAddress().getHostAddress()));
            sender.sendMessage(MessageUtils.formatInfo("&eAutenticado: " + 
                (plugin.getAuthManager().isAuthenticated(uuid) ? "&aSim" : "&cNão")));
        }
        
        // Mostrar últimas tentativas falhas
        List<Document> failedAttempts = plugin.getLogManager().getFailedAttempts(playerName, 3);
        if (!failedAttempts.isEmpty()) {
            sender.sendMessage("");
            sender.sendMessage(MessageUtils.formatInfo("&eÚltimas tentativas falhas:"));
            for (Document log : failedAttempts) {
                sender.sendMessage(MessageUtils.formatInfo("&8» &f" + log.getString("date") + " &7- &c" + log.getString("reason")));
            }
        }
        sender.sendMessage("");
    }

    private void showPlayerLogs(CommandSender sender, String playerName) {
        List<Document> logs = plugin.getLogManager().getPlayerLogs(playerName, 10);
        
        sender.sendMessage("");
        sender.sendMessage(MessageUtils.formatInfo("&6&lSensitive &8- &3&lLogs do Jogador"));
        sender.sendMessage("");
        sender.sendMessage(MessageUtils.formatInfo("&eJogador: &f" + playerName));
        sender.sendMessage(MessageUtils.formatInfo("&eÚltimos 10 logs:"));
        sender.sendMessage("");
        
        for (Document log : logs) {
            String type = log.getString("type");
            String date = log.getString("date");
            String message = "";
            
            switch (type) {
                case "LOGIN":
                    boolean success = log.getBoolean("success");
                    message = "&8» &f" + date + " &7- " + (success ? "&aLogin bem-sucedido" : "&cLogin falhou");
                    break;
                case "REGISTER":
                    message = "&8» &f" + date + " &7- &aRegistro realizado";
                    break;
                case "FAILED_ATTEMPT":
                    message = "&8» &f" + date + " &7- &c" + log.getString("reason");
                    break;
                case "ADMIN_ACTION":
                    message = "&8» &f" + date + " &7- &e" + log.getString("action") + " &7por &f" + log.getString("admin");
                    break;
            }
            sender.sendMessage(MessageUtils.formatInfo(message));
        }
        sender.sendMessage("");
    }

    private void unregisterPlayer(CommandSender sender, String playerName) {
        if (!plugin.getAuthManager().isRegistered(playerName)) {
            sender.sendMessage(MessageUtils.formatError("&6Sensitive &f| &cEste jogador não está registrado!"));
            return;
        }

        // Força o logout se estiver online
        Player target = Bukkit.getPlayer(playerName);
        if (target != null && target.isOnline()) {
            forceLogout(sender, playerName);
        }

        // Remove o registro
        plugin.getAuthManager().unregisterPlayer(playerName);
        
        // Loga a ação
        if (sender instanceof Player) {
            plugin.getLogManager().logAdminAction((Player) sender, playerName, "Removeu registro");
        }

        sender.sendMessage(MessageUtils.formatSuccess("&6Sensitive &f| &aRegistro do jogador &f" + playerName + " &aremovido com sucesso!"));
    }

    private void forceLogout(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(MessageUtils.formatError("&6Sensitive &f| &cJogador não está online!"));
            return;
        }

        UUID uuid = target.getUniqueId();
        if (!plugin.getAuthManager().isAuthenticated(uuid)) {
            sender.sendMessage(MessageUtils.formatError("&6Sensitive &f| &cEste jogador não está autenticado!"));
            return;
        }

        // Remove autenticação e sessão
        plugin.getAuthManager().removePlayer(uuid);
        plugin.getSessionManager().clearSession(target);
        
        // Loga a ação
        if (sender instanceof Player) {
            plugin.getLogManager().logAdminAction((Player) sender, playerName, "Forçou logout");
        }

        target.sendMessage(MessageUtils.formatWarning("&6Sensitive &f| &eVocê foi deslogado por um administrador!"));
        sender.sendMessage(MessageUtils.formatSuccess("&6Sensitive &f| &aJogador &f" + playerName + " &adeslogado com sucesso!"));
    }

    private void showRecentLogs(CommandSender sender) {
        List<Document> logs = plugin.getLogManager().getRecentLogs(10);
        
        sender.sendMessage("");
        sender.sendMessage(MessageUtils.formatInfo("&6&lSensitive &8- &3&lLogs Recentes"));
        sender.sendMessage("");
        sender.sendMessage(MessageUtils.formatInfo("&eÚltimos 10 logs do servidor:"));
        sender.sendMessage("");
        
        for (Document log : logs) {
            String type = log.getString("type");
            String date = log.getString("date");
            String player = log.getString("player");
            String message = "";
            
            switch (type) {
                case "LOGIN":
                    boolean success = log.getBoolean("success");
                    message = "&8» &f" + date + " &7- &f" + player + " &7- " + 
                             (success ? "&aLogin bem-sucedido" : "&cLogin falhou");
                    break;
                case "REGISTER":
                    message = "&8» &f" + date + " &7- &f" + player + " &7- &aRegistro realizado";
                    break;
                case "FAILED_ATTEMPT":
                    message = "&8» &f" + date + " &7- &f" + player + " &7- &c" + log.getString("reason");
                    break;
                case "ADMIN_ACTION":
                    message = "&8» &f" + date + " &7- &f" + log.getString("admin") + 
                             " &7- &e" + log.getString("action") + " &7em &f" + log.getString("target");
                    break;
            }
            sender.sendMessage(MessageUtils.formatInfo(message));
        }
        sender.sendMessage("");
    }
} 