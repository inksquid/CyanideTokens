package org.cyanidemc.cyanidetokens;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.cyanidemc.cyanidetokens.actions.ActionRunner;
import org.cyanidemc.cyanidetokens.data.Config;
import org.cyanidemc.cyanidetokens.data.PlayerConfig;
import org.cyanidemc.cyanidetokens.data.wrappers.InventoryWrapper;
import org.cyanidemc.cyanidetokens.data.wrappers.ItemWrapper;
import org.cyanidemc.cyanidetokens.events.TokenBlocksUpdateEvent;
import org.cyanidemc.cyanidetokens.handlers.ConfigHandler;
import org.cyanidemc.cyanidetokens.lib.ProtocolHandler;
import org.cyanidemc.cyanidetokens.util.CyanideUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class CyanideTokens extends JavaPlugin implements Listener {

    @Getter private static CyanideTokens instance;
    @Getter private static ConfigHandler inventories;
    @Getter private static PlayerConfig players;
    // @Getter private static MongoDatabase mongo;
    @Getter private static ProtocolHandler protocolHandler;


    @Override
    public void onEnable() {
        instance = this;
        getConfig().options().copyDefaults(true);
        saveConfig();

        if (getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            protocolHandler = new ProtocolHandler();
        }
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void reloadConfig() {
        File folder = getDataFolder();

        super.reloadConfig();
        inventories = new ConfigHandler(folder, "inventories.yml");
        players = new PlayerConfig(folder, "players.yml");
        Config.loadConfig(getConfig());
		/*mongo = new MongoDatabase();
		
		mongo.connect(Config.getSettings().getConfigurationSection("mongodb"));*/
    }

    public static boolean hasProtocolHandler() {
        return protocolHandler != null;
    }

    public static void openInventory(Player player, String name) {
        InventoryWrapper inventory = Config.getInventory(name);

        if (inventory != null) {
            player.closeInventory();
            player.openInventory(inventory.getInventory(player));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            InventoryWrapper wrapper = Config.getTitle(event.getInventory().getTitle());

            if (wrapper != null) {
                ItemWrapper item = wrapper.getItem(event.getSlot());

                event.setCancelled(true);

                if (item != null) {
                    Player player = (Player) event.getWhoClicked();

                    ActionRunner.runActionsAsync(player, item);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            ((Player) event.getPlayer()).updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.getInventory().getItemInHand() == null) {
            return;
        }

        if (isTokenItem(player.getInventory().getItemInHand())) {
            int amount = player.getInventory().getItemInHand().getAmount();

            String uuid = player.getUniqueId().toString();
            players.addPlayer(uuid, amount, false);

            player.sendMessage(Config.getReceiveMessage().replace("<player>", "a token").replace("<amount>", String.valueOf(amount))
                    .replace("<number>", String.valueOf(players.getPlayer(uuid)/*mongo.getTokens(targetUuid)*/)));

            player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        int blocks = (player.hasMetadata("tokenBlocks") ? player.getMetadata("tokenBlocks").get(0).asInt() : 0) + 1;

        if (blocks == Config.getBlockBreaks()) {
            //mongo.addTokens(uuid, 1);
            players.addPlayer(uuid, 1, false);
            player.removeMetadata("tokenBlocks", this);
            player.sendMessage(Config.getMineMessage().replace("<amount>", String.valueOf(players.getPlayer(uuid)/*mongo.getTokens(uuid)*/)));
        } else {
            player.setMetadata("tokenBlocks", new FixedMetadataValue(this, blocks));
        }

        getServer().getPluginManager().callEvent(new TokenBlocksUpdateEvent(player, blocks));
    }

    public void sendTokenMessage(CommandSender sender) {
        String tokens = String.valueOf(sender instanceof Player ? players.getPlayer(((Player) sender).getUniqueId().toString())/*mongo.getTokens(((Player) sender).getUniqueId().toString())*/ : 0);

        for (String message : Config.getHelpMessage()) {
            sender.sendMessage(message.replace("<tokens>", tokens));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        new Thread(() -> {
            if (args.length == 0) {
                sendTokenMessage(sender);
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("cyanidetokens.reload")) {
                    reloadConfig();
                    sender.sendMessage(Config.getReloadMessage());
                } else {
                    sender.sendMessage(Config.getNoPermMessage());
                }
            } else if (args[0].equalsIgnoreCase("give")) {
                if (sender.hasPermission("cyanidetokens.give")) {
                    if (args.length < 3) {
                        sender.sendMessage(Config.getGiveHelpMessage());
                    } else {
                        Player player = getServer().getPlayer(args[1]);

                        if (player != null) {
                            int amount = CyanideUtil.parseInteger(args[2], 0);
                            String uuid = player.getUniqueId().toString();

                            //mongo.addTokens(uuid, amount);
                            players.addPlayer(uuid, amount, true);
                            sender.sendMessage(Config.getGiveMessage().replace("<player>", player.getName()).replace("<amount>", String.valueOf(amount)).replace("<number>", String.valueOf(players.getPlayer(uuid)/*mongo.getTokens(uuid)*/)));
                        } else {
                            sender.sendMessage(Config.getNotOnlineMessage().replace("<player>", args[1]));
                        }
                    }
                } else {
                    sender.sendMessage(Config.getNoPermMessage());
                }
            } else if (args[0].equalsIgnoreCase("take")) {
                if (sender.hasPermission("cyanidetokens.take")) {
                    if (args.length < 3) {
                        sender.sendMessage(Config.getTakeHelpMessage());
                    } else {
                        Player player = getServer().getPlayer(args[1]);

                        if (player != null) {
                            int amount = CyanideUtil.parseInteger(args[2], 0);
                            String uuid = player.getUniqueId().toString();

                            //mongo.takeTokens(uuid, amount);
                            players.takePlayer(uuid, amount, true);
                            sender.sendMessage(Config.getTakeMessage().replace("<player>", args[1]).replace("<amount>", String.valueOf(amount)).replace("<number>", String.valueOf(players.getPlayer(uuid)/*mongo.getTokens(uuid)*/)));
                        } else {
                            sender.sendMessage(Config.getNotOnlineMessage().replace("<player>", args[1]));
                        }
                    }
                } else {
                    sender.sendMessage(Config.getNoPermMessage());
                }
            } else if (args[0].equalsIgnoreCase("withdraw")) {
                if (sender.hasPermission("cyanidetokens.withdraw")) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;

                        if (args.length == 1) {
                            sender.sendMessage(Config.getWithdrawHelpMessage());
                        } else {

                            Bukkit.getScheduler().scheduleSyncDelayedTask(CyanideTokens.this, () -> {
                                try {
                                    String uuid = player.getUniqueId().toString();
                                    int withdraw = Integer.parseInt(args[1]);
                                    int original_amount = withdraw;
                                    if (withdraw <= 0) {
                                        player.sendMessage(Config.getCantSendMessage());
                                    } else if (withdraw > players.getPlayer(uuid)) {
                                        player.sendMessage(Config.getNotEnoughMessage());
                                    } else if (!(enoughRoom(player, withdraw))) {
                                        player.sendMessage(Config.getNotEnoughInventorySpace());
                                    } else {
                                        List<ItemStack> add = new ArrayList<>();
                                        while (withdraw > 0) {
                                            ItemStack stack = Config.getToken_item().clone();

                                            if (withdraw >= 64) {
                                                stack.setAmount(64);
                                                add.add(stack);
                                                withdraw -= 64;
                                            } else {
                                                stack.setAmount(withdraw);
                                                add.add(stack);
                                                withdraw = 0;
                                            }
                                        }

                                        player.getInventory().addItem(add.toArray(new ItemStack[0]));
                                        players.takePlayer(uuid, original_amount, false);
                                    }
                                } catch (NumberFormatException ex) {
                                    sender.sendMessage(Config.getWithdrawHelpMessage());
                                }
                            });
                        }
                    } else {
                        sender.sendMessage(Config.getConsoleMessage());
                    }
                } else {
                    sender.sendMessage(Config.getNoPermMessage());
                }
            } else if (args[0].equalsIgnoreCase("send")) {
                if (sender.hasPermission("cyanidetokens.send")) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;

                        if (args.length < 3) {
                            player.sendMessage(Config.getSendHelpMessage());
                        } else {
                            int amount = Math.max(CyanideUtil.parseInteger(args[2], 1), 0);

                            if (amount > 0) {
                                String uuid = player.getUniqueId().toString();
                                int tokens = players.getPlayer(uuid);/*mongo.getTokens(uuid);*/
                                amount = Math.min(amount, tokens);

                                if (amount > 0) {
                                    Player target = getServer().getPlayer(args[1]);

                                    if (target != null) {
                                        String targetUuid = target.getUniqueId().toString();
                						            
                                        /*mongo.takeTokens(uuid, amount);
                                        mongo.addTokens(targetUuid, amount);*/
                                        players.takePlayer(uuid, amount, true);
                                        players.addPlayer(targetUuid, amount, true);

                                        player.sendMessage(Config.getSendMessage().replace("<player>", target.getName()).replace("<amount>", String.valueOf(amount)).replace("<number>", String.valueOf(tokens - amount)));
                                        target.sendMessage(Config.getReceiveMessage().replace("<player>", player.getName()).replace("<amount>", String.valueOf(amount)).replace("<number>", String.valueOf(players.getPlayer(targetUuid)/*mongo.getTokens(targetUuid)*/)));
                                    }
                                } else {
                                    sender.sendMessage(Config.getCantSendMessage());
                                }
                            } else {
                                sender.sendMessage(Config.getCantSendMessage());
                            }
                        }
                    } else {
                        sender.sendMessage(Config.getConsoleMessage());
                    }
                } else {
                    sender.sendMessage(Config.getNoPermMessage());
                }
            } else if (args[0].equalsIgnoreCase("shop")) {
                if (sender.hasPermission("cyanidetokens.shop")) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;

                        openInventory(player, Config.getDefaultShop());
                    } else {
                        sender.sendMessage(Config.getConsoleMessage());
                    }
                } else {
                    sender.sendMessage(Config.getNoPermMessage());
                }
            } else {
                sendTokenMessage(sender);
            }
        }).start();

        return true;
    }

    private static boolean enoughRoom(Player player, int amount) {
        int left = amount;

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack at = player.getInventory().getItem(i);

            if (at == null) {
                left -= 64;
            }

            if (isTokenItem(at)) {
                left -= (64 - at.getAmount());
            }
        }

        return left <= 0;
    }

    private static boolean isTokenItem(ItemStack stack) {
        if (stack == null) {
            return false;
        }

        if (!stack.getType().equals(Config.getToken_item().getType())) {
            return false;
        }

        if (Config.getToken_item().hasItemMeta() && !stack.hasItemMeta()) {
            return false;
        }

        if (Config.getToken_item().getItemMeta().hasDisplayName() && !stack.getItemMeta().hasDisplayName()) {
            return false;
        }

        if (!Config.getToken_item().getItemMeta().getDisplayName().equals(stack.getItemMeta().getDisplayName())) {
            return false;
        }

        if (Config.getToken_item().getItemMeta().hasLore() && !stack.getItemMeta().hasLore()) {
            return false;
        }

        if (!Config.getToken_item().getItemMeta().getLore().equals(stack.getItemMeta().getLore())) {
            return false;
        }

        return true;
    }
}
