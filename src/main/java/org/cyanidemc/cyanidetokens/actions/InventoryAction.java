package org.cyanidemc.cyanidetokens.actions;

import org.bukkit.entity.Player;
import org.cyanidemc.cyanidetokens.CyanideTokens;
import org.cyanidemc.cyanidetokens.data.wrappers.ItemWrapper;

public class InventoryAction implements Action {

    @Override
    public boolean runAction(Player player, ItemWrapper item) {
        if (item.hasInventory()) {
            CyanideTokens.openInventory(player, item.getInventory());
            return false;
        }

        return true;
    }
}
