package org.cyanidemc.cyanidetokens.data;

import org.cyanidemc.cyanidetokens.CyanideTokens;
import org.cyanidemc.cyanidetokens.events.TokenBalanceUpdateEvent;
import org.cyanidemc.cyanidetokens.handlers.ConfigHandler;

import java.io.File;

public class PlayerConfig extends ConfigHandler {

    public PlayerConfig(File folder, String name) {
        super(folder, name);
    }


    public int getPlayer(String uuid) {
        return getConfig().getInt(uuid);
    }

    public void addPlayer(String uuid, int amount, boolean async) {
        setPlayer(uuid, getPlayer(uuid) + Math.max(amount, 0), async);
    }

    public void takePlayer(String uuid, int amount, boolean async) {
        setPlayer(uuid, getPlayer(uuid) - Math.max(amount, 0), async);
    }

    public void setPlayer(String uuid, int amount, boolean async) {
        amount = Math.max(amount, 0);
        set(uuid, amount, true);
        CyanideTokens.getInstance().getServer().getPluginManager().callEvent(new TokenBalanceUpdateEvent(async, uuid, amount));
    }
}
