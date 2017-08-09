package org.cyanidemc.cyanidetokens.data.wrappers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Synchronized;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.cyanidemc.cyanidetokens.util.CyanideUtil;

import java.util.Map;

@AllArgsConstructor
public class InventoryWrapper {

    private Inventory inventory;
    @Getter private Map<Integer, ItemWrapper> items;

    public Inventory getInventory(Player player) {
        return CyanideUtil.cloneInventory(inventory, player);
    }

    public Inventory getInventory() {
        return getInventory(null);
    }

    @Synchronized
    public ItemWrapper getItem(int slot) {
        return items.get(slot);
    }
}
