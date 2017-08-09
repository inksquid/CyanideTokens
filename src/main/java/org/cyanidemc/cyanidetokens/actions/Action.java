package org.cyanidemc.cyanidetokens.actions;

import org.bukkit.entity.Player;
import org.cyanidemc.cyanidetokens.data.wrappers.ItemWrapper;

public interface Action {

    public boolean runAction(Player player, ItemWrapper item);
}
