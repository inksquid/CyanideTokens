package org.cyanidemc.cyanidetokens.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.cyanidemc.cyanidetokens.CyanideTokens;

import java.util.List;

public class CyanideUtil {

    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static List<String> color(List<String> list) {
        for (int i = 0, s = list.size(); i < s; i++) {
            list.set(i, color(list.get(i)));
        }

        return list;
    }

    public static String limit(String string, int limit) {
        return string.substring(0, Math.min(string.length(), limit));
    }

    public static int parseInteger(String string, int fallback) {
        try {
            return Integer.valueOf(string);
        } catch (Exception e) {
            return fallback;
        }
    }

    public static Inventory cloneInventory(Inventory inventory, Player player) {
        String title = inventory.getTitle();

        if (player != null) {
            title = String.format("%s%s", title, CyanideTokens.getPlayers().getPlayer(player.getUniqueId().toString()));
        }

        Inventory clone = Bukkit.createInventory(null, inventory.getSize(), title);

        clone.setContents(inventory.getContents());
        return clone;
    }

    public static String convertToEnum(String string) {
        return string.toUpperCase().replace(" ", "_");
    }

    public static Material getMaterial(String material) {
        return material == null ? null : Material.getMaterial(convertToEnum(material));
    }

    public static void executeCommands(Player player, List<String> commands) {
        if (!commands.isEmpty()) {
            String name = player.getName();

            for (String command : commands) {
                command = command.replace("<name>", name);

                if (command.startsWith("c:")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.substring(2));
                } else {
                    Bukkit.dispatchCommand(player, command);
                }
            }
        }
    }
}
