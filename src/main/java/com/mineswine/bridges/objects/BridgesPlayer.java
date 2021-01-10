package com.mineswine.bridges.objects;

import com.mineswine.api.gameapi.objects.GamePlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dylangellis on 2/8/15.
 */
public class BridgesPlayer extends GamePlayer {
    private boolean diedBefore;
    private List<Location> claimed = new ArrayList<Location>();

    public BridgesPlayer(Player player) {
        super(player);
    }

    public boolean hasDiedBefore() {
        return diedBefore;
    }

    public List<Location> getClaimed() {
        return claimed;
    }

    public void setDiedBefore(boolean diedBefore) {
        this.diedBefore = diedBefore;
    }
}
