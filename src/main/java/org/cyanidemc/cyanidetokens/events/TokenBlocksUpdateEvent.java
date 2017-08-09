package org.cyanidemc.cyanidetokens.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TokenBlocksUpdateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    @Getter private Player player;
    @Getter private int blocks;

    public TokenBlocksUpdateEvent(Player player, int blocks) {
        this.player = player;
        this.blocks = blocks;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
