package com.mineswine.bridges.config;

import com.mineswine.api.gameapi.configuration.GameConfig;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dylangellis on 1/23/15.
 */
public class BridgesGameConfig extends GameConfig {

    private int bridgeTime = 120;
    private int maxMobSpawnTime = 80;
    private int minMobSpawnTime = 60;
    private String[] claimBlocks = new String[]{"CHEST", "WORKBENCH", "FURNACE"};
    private Map<String, Object> kitAbilityConfigs = new HashMap<String, Object>();
    public OreData coal = new OreData(Material.COAL_ORE.getId());
    public OreData iron = new OreData(Material.IRON_ORE.getId());
    public OreData gold = new OreData(Material.GOLD_ORE.getId());
    public OreData lapis = new OreData(Material.LAPIS_ORE.getId());
    public OreData redstone = new OreData(Material.REDSTONE_ORE.getId());
    public OreData emerald = new OreData(Material.EMERALD_ORE.getId());
    public OreData diamond = new OreData(Material.DIAMOND_ORE.getId());



    public void onLoad(){
        loadSubconfigs(kitAbilityConfigs, "BridgesKits", com.mineswine.bridges.kits.BridgesKits.values());
    }

    public int getMaxMobSpawnTime() {
        return maxMobSpawnTime;
    }

    public int getMinMobSpawnTime() {
        return minMobSpawnTime;
    }

    public int getBridgeTime() {
        return bridgeTime;
    }

    public void setBridgeTime(int bridgeTime) {
        this.bridgeTime = bridgeTime;
    }

    public String[] getClaimBlocks() {
        return claimBlocks;
    }

    public <T> T getKitConfig(Class<T> clazz) {
        return (T) kitAbilityConfigs.get(clazz.getSimpleName());
    }


    public OreData[] getOres() {
        return new OreData[]{coal, iron, gold, lapis, redstone, emerald, diamond};
    }

    public class OreData {
        public int maxVein = 8;
        public int minVein = 1;
        public int percentSpawn = 50;
        public int veinDistance = 5;
        public int depth = 100;
        public int exp = 10;
        public int __id__ = 0;

        public OreData(int blockId) {
            this.__id__ = blockId;
        }
    }

    public static class BridgesKits {
        public static class baker {
            public int cookieTime = 100;
        }

        public static class scavenger {
            public int resourcePercent = 30;
        }

        public static class enchanter {
            public int levelGainTime = 100;
        }

        public static class wizard {
            public int wizardBookTime = 100;
            public int lightningRange = 16;
            public int max = 2;
            public double fireballVelocity = 1.0;
            public String[] books = new String[]{"item.wizard.firebook", "item.wizard.lightningbook", "item.wizard.healingbook"};
        }

        public static class bomber {
            public int tntTime = 100;
            public int max = 2;
        }

        public static class acrobat {
            public int doubleJumpTickTime = 30;
            public double doubleJumpVelocityMultiplier = 0.3, doubleJumpVelocityY = 0.93;
        }

        public static class archer {
            public int arrowTime = 100;
        }

        public static class boxer {
            public int slowTime = 2;
        }
    }
}
