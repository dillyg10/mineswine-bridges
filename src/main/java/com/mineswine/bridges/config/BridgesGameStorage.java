package com.mineswine.bridges.config;

import com.mineswine.api.gameapi.configuration.GameConfig;
import com.mineswine.api.gameapi.storage.GameStorage;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by dylangellis on 2/4/15.
 */
public class BridgesGameStorage extends GameStorage {
    public BridgesGameStorage(JavaPlugin plugin, GameConfig config) {
        super(plugin, config);
    }

    @Override
    public void firstRead() {

    }

    @Override
    public void reloaded() {

    }
}
