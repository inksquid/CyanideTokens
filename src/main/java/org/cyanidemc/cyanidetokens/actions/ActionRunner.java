package org.cyanidemc.cyanidetokens.actions;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.cyanidemc.cyanidetokens.data.wrappers.ItemWrapper;

public class ActionRunner {

    @Getter private static Action[] actions = new Action[] {new InventoryAction(), new PriceAction(), new CommandAction()};

    public static void runActionsSync(Player player, ItemWrapper item) {
        for (Action action : actions) {
            if (!action.runAction(player, item)) {
                return;
            }
        }
    }

    public static void runActionsAsync(Player player, ItemWrapper item) {
        new Thread(() -> runActionsSync(player, item)).start();
    }
}
