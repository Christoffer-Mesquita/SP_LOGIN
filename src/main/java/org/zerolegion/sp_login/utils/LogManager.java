package org.zerolegion.sp_login.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.zerolegion.sp_login.SP_LOGIN;

import java.util.Arrays;

public class LogManager {
    private static final String[] STARTUP_ART = {
        "   ____                _ __  _           ",
        "  / __/__ ___  ___ _  (_) /_(_)  _____ ",
        " _\\ \\/ -_) _ \\/ _ `/ / / __/ / |/ / -_)",
        "/___/\\__/_//_/\\_, / /_/\\__/_/|___/\\__/ ",
        "             /___/                      "
    };

    private static final String[] SHUTDOWN_ART = {
        "   ____                _ __  _           ",
        "  / __/__ ___  ___ _  (_) /_(_)  _____ ",
        " _\\ \\/ -_) _ \\/ _ `/ / / __/ / |/ / -_)",
        "/___/\\__/_//_/\\_, / /_/\\__/_/|___/\\__/ ",
        "             /___/                      "
    };

    public static void logStartup(SP_LOGIN plugin) {
        Bukkit.getConsoleSender().sendMessage("");
        for (String line : STARTUP_ART) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + line);
        }
        Bukkit.getConsoleSender().sendMessage("");
        
        String separator = ChatColor.DARK_GRAY + "----------------------------------------";
        Bukkit.getConsoleSender().sendMessage(separator);
        
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "» " + ChatColor.WHITE + "Plugin: " + 
                                            ChatColor.YELLOW + "Sensitive Prison - Auth");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "» " + ChatColor.WHITE + "Versão: " + 
                                            ChatColor.YELLOW + plugin.getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "» " + ChatColor.WHITE + "Autor: " + 
                                            ChatColor.YELLOW + "ZeroLegion");
        Bukkit.getConsoleSender().sendMessage(separator);
        
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "» " + ChatColor.WHITE + "Status do Sistema:");
        
        // Status do MongoDB
        boolean mongoStatus = plugin.getMongoDB() != null;
        String mongoStatusText = mongoStatus ? 
                ChatColor.GREEN + "Conectado" : 
                ChatColor.RED + "Desconectado";
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "  ⤷ " + ChatColor.WHITE + "MongoDB: " + mongoStatusText);
        
        // Status dos Comandos
        boolean commandsStatus = plugin.getCommand("login") != null && plugin.getCommand("register") != null;
        String commandsStatusText = commandsStatus ? 
                ChatColor.GREEN + "Registrados" : 
                ChatColor.RED + "Erro ao Registrar";
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "  ⤷ " + ChatColor.WHITE + "Comandos: " + commandsStatusText);
        
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Plugin iniciado com sucesso!");
        Bukkit.getConsoleSender().sendMessage(separator);
        Bukkit.getConsoleSender().sendMessage("");
    }

    public static void logShutdown(SP_LOGIN plugin) {
        String separator = ChatColor.DARK_GRAY + "----------------------------------------";
        Bukkit.getConsoleSender().sendMessage("");
        for (String line : SHUTDOWN_ART) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + line);
        }
        Bukkit.getConsoleSender().sendMessage("");
        
        Bukkit.getConsoleSender().sendMessage(separator);
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "» " + ChatColor.WHITE + "Desligando sistemas...");
        
        // Status de Desligamento do MongoDB
        String mongoStatus = plugin.getMongoDB() != null ? 
                ChatColor.GREEN + "Conexão fechada com sucesso" : 
                ChatColor.YELLOW + "Nenhuma conexão ativa";
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "  ⤷ " + ChatColor.WHITE + "MongoDB: " + mongoStatus);
        
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Plugin desativado com sucesso!");
        Bukkit.getConsoleSender().sendMessage(separator);
        Bukkit.getConsoleSender().sendMessage("");
    }
} 