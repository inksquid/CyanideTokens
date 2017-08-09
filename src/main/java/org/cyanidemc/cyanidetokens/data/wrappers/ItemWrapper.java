package org.cyanidemc.cyanidetokens.data.wrappers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
public class ItemWrapper {

    @Getter private String inventory;
    @Getter private int tokens;
    @Getter private List<String> commands;

    public boolean hasInventory() {
        return inventory != null && !inventory.isEmpty();
    }

    public boolean hasTokens() {
        return tokens > 0;
    }

    public boolean hasCommands() {
        return commands != null && !commands.isEmpty();
    }
}
