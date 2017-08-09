package org.cyanidemc.cyanidetokens.actions;

import org.bukkit.entity.Player;
import org.cyanidemc.cyanidetokens.data.wrappers.ItemWrapper;
import org.cyanidemc.cyanidetokens.util.CyanideUtil;

public class CommandAction implements Action {

    @Override
    public boolean runAction(Player player, ItemWrapper item) {
        if (item.hasCommands()) {
            CyanideUtil.executeCommands(player, item.getCommands());
        }

        return true;
    }
}
