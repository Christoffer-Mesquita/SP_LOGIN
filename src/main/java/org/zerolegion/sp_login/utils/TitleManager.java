package org.zerolegion.sp_login.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class TitleManager {
    
    public static void sendLoginTitle(Player player) {
        if (player == null || !player.isOnline()) return;
        
        String title = ChatColor.GOLD + "" + ChatColor.BOLD + "Sensitive" + ChatColor.DARK_GRAY + " - " + 
                      ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Prison";
        String subtitle = ChatColor.YELLOW + "Use " + ChatColor.WHITE + "/login <senha>" + 
                         ChatColor.YELLOW + " para entrar";
        
        sendTitle(player, title, subtitle);
    }
    
    public static void sendRegisterTitle(Player player) {
        if (player == null || !player.isOnline()) return;
        
        String title = ChatColor.GOLD + "" + ChatColor.BOLD + "Sensitive" + ChatColor.DARK_GRAY + " - " + 
                      ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Prison";
        String subtitle = ChatColor.YELLOW + "Use " + ChatColor.WHITE + "/register <senha> <senha>" + 
                         ChatColor.YELLOW + " para se registrar";
        
        sendTitle(player, title, subtitle);
    }
    
    public static void clearTitle(Player player) {
        if (player == null || !player.isOnline()) return;
        sendTitle(player, "", "");
    }
    
    private static void sendTitle(Player player, String title, String subtitle) {
        try {
            if (player == null || !player.isOnline()) return;

            Class<?> packetPlayOutTitleClass = getNMSClass("PacketPlayOutTitle");
            Class<?> iChatBaseComponentClass = getNMSClass("IChatBaseComponent");
            Class<?> chatSerializerClass = null;
            
            for (Class<?> clazz : iChatBaseComponentClass.getDeclaredClasses()) {
                if (clazz.getSimpleName().equals("ChatSerializer")) {
                    chatSerializerClass = clazz;
                    break;
                }
            }
            
            if (packetPlayOutTitleClass == null || chatSerializerClass == null) return;

            Method a = chatSerializerClass.getDeclaredMethod("a", String.class);
            Object titleComponent = a.invoke(null, "{\"text\": \"" + title + "\"}");
            Object subtitleComponent = a.invoke(null, "{\"text\": \"" + subtitle + "\"}");

            Constructor<?> titleConstructor = packetPlayOutTitleClass.getConstructor(
                    packetPlayOutTitleClass.getDeclaredClasses()[0],
                    iChatBaseComponentClass,
                    int.class, int.class, int.class);

            Object titlePacket = titleConstructor.newInstance(
                    packetPlayOutTitleClass.getDeclaredClasses()[0].getField("TITLE").get(null),
                    titleComponent, 10, 70, 20);
            
            Object subtitlePacket = titleConstructor.newInstance(
                    packetPlayOutTitleClass.getDeclaredClasses()[0].getField("SUBTITLE").get(null),
                    subtitleComponent, 10, 70, 20);

            sendPacket(player, titlePacket);
            sendPacket(player, subtitlePacket);
        } catch (Exception e) {
            // Ignora silenciosamente para não spammar o console
        }
    }

    private static Class<?> getNMSClass(String name) {
        try {
            return Class.forName("net.minecraft.server." + getVersion() + "." + name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static String getVersion() {
        return org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    private static void sendPacket(Player player, Object packet) {
        try {
            if (player == null || !player.isOnline()) return;
            
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            if (handle == null) return;
            
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            if (playerConnection == null) return;
            
            Method sendPacket = playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet"));
            if (sendPacket == null) return;
            
            sendPacket.invoke(playerConnection, packet);
        } catch (Exception e) {
            // Ignora silenciosamente para não spammar o console
        }
    }
} 