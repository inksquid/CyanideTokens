package org.cyanidemc.cyanidetokens.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TokenBalanceUpdateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    @Getter private String uuid;
    @Getter private int tokens;

    public TokenBalanceUpdateEvent(boolean async, String uuid, int tokens) {
        super(async);
        this.uuid = uuid;
        this.tokens = tokens;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
