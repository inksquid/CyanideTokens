package org.cyanidemc.cyanidetokens.data;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.cyanidemc.cyanidetokens.CyanideTokens;
import org.cyanidemc.cyanidetokens.data.wrappers.InventoryWrapper;
import org.cyanidemc.cyanidetokens.data.wrappers.ItemWrapper;
import org.cyanidemc.cyanidetokens.util.CyanideUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Config {

    @Getter
    private static ConfigurationSection general;
    @Getter private static ConfigurationSection settings;
    @Getter private static String reloadMessage;
    @Getter private static String noPermMessage;
    @Getter private static String giveHelpMessage;
    @Getter private static String takeHelpMessage;
    @Getter private static String sendHelpMessage;
    @Getter private static String withdrawHelpMessage;
    @Getter private static String notEnoughInventorySpace;
    @Getter private static String giveMessage;
    @Getter	private static String takeMessage;
    @Getter private static String sendMessage;
    @Getter private static String receiveMessage;
    @Getter private static String notEnoughMessage;
    @Getter private static String boughtMessage;
    @Getter private static String mineMessage;
    @Getter private static String cantSendMessage;
    @Getter private static String notOnlineMessage;
    @Getter private static String consoleMessage;
    @Getter private static List<String> helpMessage;
    @Getter private static String defaultShop;
    @Getter private static int blockBreaks;
    @Getter private static Map<String, InventoryWrapper> inventories = new ConcurrentHashMap<String, InventoryWrapper>();
    @Getter private static Map<String, InventoryWrapper> titles = new ConcurrentHashMap<String, InventoryWrapper>();
    @Getter private static ItemStack token_item;

    public static void loadConfig(FileConfiguration config) {
        general = config.getConfigurationSection("general");
        settings = config.getConfigurationSection("settings");
        reloadMessage = CyanideUtil.color(general.getString("reloadMessage"));
        noPermMessage = CyanideUtil.color(general.getString("noPermMessage"));
        giveHelpMessage = CyanideUtil.color(general.getString("giveHelpMessage"));
        takeHelpMessage = CyanideUtil.color(general.getString("takeHelpMessage"));
        sendHelpMessage = CyanideUtil.color(general.getString("sendHelpMessage"));
        withdrawHelpMessage = CyanideUtil.color(general.getString("withdrawHelpMessage"));
        notEnoughInventorySpace = CyanideUtil.color(general.getString("notEnoughInventorySpace"));
        giveMessage = CyanideUtil.color(general.getString("giveMessage"));
        takeMessage = CyanideUtil.color(general.getString("takeMessage"));
        sendMessage = CyanideUtil.color(general.getString("sendMessage"));
        receiveMessage = CyanideUtil.color(general.getString("receiveMessage"));
        notEnoughMessage = CyanideUtil.color(general.getString("notEnoughMessage"));
        boughtMessage = CyanideUtil.color(general.getString("boughtMessage"));
        mineMessage = CyanideUtil.color(general.getString("mineMessage"));
        cantSendMessage = CyanideUtil.color(general.getString("cantSendMessage"));
        notOnlineMessage = CyanideUtil.color(general.getString("notOnlineMessage"));
        consoleMessage = CyanideUtil.color(general.getString("consoleMessage"));
        helpMessage = CyanideUtil.color(general.getStringList("helpMessage"));
        defaultShop = settings.getString("defaultShop");
        blockBreaks = settings.getInt("blockBreaks");

        token_item = new ItemStack(general.getInt("tokenItem"));

        ItemMeta meta = token_item.getItemMeta();
        if (general.contains("tokenName")) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', general.getString("tokenName")));
        }

        if (general.contains("tokenLore")) {
            List<String> lore = general.getStringList("tokenLore");
            List<String> translated = lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
            meta.setLore(translated);
        }

        token_item.setItemMeta(meta);

        loadInventories(CyanideTokens.getInventories().getConfig());
    }

    public static void loadInventories(FileConfiguration config) {
        inventories.clear();
        titles.clear();

        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);

            if (section != null) {
                Inventory inventory = Bukkit.createInventory(null, section.getInt("rows", 1) * 9, CyanideUtil.limit(CyanideUtil.color(section.getString("title", "Unknown")), 32));
                Map<Integer, ItemWrapper> items = new HashMap<Integer, ItemWrapper>();

                for (int slot = 1, s = inventory.getSize(); slot <= s; slot++) {
                    ConfigurationSection itemSection = section.getConfigurationSection(String.valueOf(slot));

                    if (itemSection != null) {
                        Material material = CyanideUtil.getMaterial(itemSection.getString("material"));

                        if (material != null && material != Material.AIR) {
                            String name = CyanideUtil.color(itemSection.getString("name"));
                            List<String> lore = CyanideUtil.color(itemSection.getStringList("lore"));
                            ItemStack item = new ItemStack(material, 1, (short) itemSection.getInt("damage"));
                            ItemMeta meta = item.getItemMeta();

                            if (name != null && !name.isEmpty()) {
                                meta.setDisplayName(name);
                            }

                            if (lore != null && !lore.isEmpty()) {
                                meta.setLore(lore);
                            }

                            item.setItemMeta(meta);

                            if (itemSection.getBoolean("enchant")) {
                                item.addUnsafeEnchantment(Enchantment.WATER_WORKER, 4);
                            }

                            inventory.setItem(slot - 1, item);
                            items.put(slot - 1, new ItemWrapper(itemSection.getString("inventory"), itemSection.getInt("tokens"), itemSection.getStringList("commands")));
                        }
                    }
                }

                InventoryWrapper wrapper = new InventoryWrapper(inventory, items);

                inventories.put(key, wrapper);
                titles.put(inventory.getTitle(), wrapper);
            }
        }
    }

    public static InventoryWrapper getInventory(String name) {
        return inventories.get(name);
    }

    public static InventoryWrapper getTitle(String title) {
        for (Map.Entry<String, InventoryWrapper> entry : titles.entrySet()) {
            if (title.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }
}
