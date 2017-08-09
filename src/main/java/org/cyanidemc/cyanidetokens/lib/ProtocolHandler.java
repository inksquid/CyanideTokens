package org.cyanidemc.cyanidetokens.lib;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import org.cyanidemc.cyanidetokens.CyanideTokens;

public class ProtocolHandler {

    public ProtocolHandler() {
        CyanideTokens plugin = CyanideTokens.getInstance();

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, Server.SET_SLOT, Server.WINDOW_ITEMS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                if (event.getPacketType().equals(Server.SET_SLOT)) {
                    modifyItem(packet.getItemModifier().read(0));
                } else {
                    modifyItem(packet.getItemArrayModifier().read(0));
                }
            }
        });

        plugin.getLogger().info("Hooked into ProtocolLib for enchantments!");
    }

    public void modifyItem(ItemStack item) {
        if (item != null) {
            NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(item);

            compound.put(NbtFactory.ofList("AttributeModifiers"));

            if (item.getEnchantmentLevel(Enchantment.WATER_WORKER) == 4) {
                compound.put(NbtFactory.ofList("ench"));
            }
        }
    }

    public void modifyItem(ItemStack[] items) {
        for (ItemStack item : items) {
            modifyItem(item);
        }
    }
}
