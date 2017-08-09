package org.cyanidemc.cyanidetokens.actions;

import org.bukkit.entity.Player;
import org.cyanidemc.cyanidetokens.CyanideTokens;
import org.cyanidemc.cyanidetokens.data.Config;
import org.cyanidemc.cyanidetokens.data.wrappers.ItemWrapper;

public class PriceAction implements Action {

    @Override
    public boolean runAction(Player player, ItemWrapper item) {
        player.closeInventory();

        if (!item.hasTokens()) {
            return true;
        } else {
            String uuid = player.getUniqueId().toString();
            int price = item.getTokens();
            int tokens = CyanideTokens.getPlayers().getPlayer(uuid);
            //CyanideTokens.getMongo().getTokens(uuid);

            if (tokens >= price) {
                //CyanideTokens.getMongo().takeTokens(uuid, price);
                CyanideTokens.getPlayers().takePlayer(uuid, price, true);
                player.sendMessage(Config.getBoughtMessage().replace("<cost>", String.valueOf(price)).replace("<remaining>", String.valueOf(tokens - price)));
                return true;
            } else {
                player.sendMessage(Config.getNotEnoughMessage().replace("<required>", String.valueOf(price - tokens)));
            }
        }

        return false;
    }
}
