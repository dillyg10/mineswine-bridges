package com.mineswine.bridges;

import com.mineswine.api.commands.temp.CommandManager;
import com.mineswine.api.gameapi.GameAPI;
import com.mineswine.api.gameapi.gamehandler.GameTeam;
import com.mineswine.api.gameapi.gamehandler.GameTeam.TeamType;
import com.mineswine.api.gameapi.gamemap.GameMap;
import com.mineswine.api.gameapi.language.Language;
import com.mineswine.api.gameapi.objects.GamePlayer;
import com.mineswine.api.gameapi.objects.Kit;
import com.mineswine.api.gameapi.objects.ObjectsInitializer;
import com.mineswine.api.gameapi.storage.LanguageFile;
import com.mineswine.bridges.commands.CommandTest;
import com.mineswine.bridges.config.BridgesGameConfig;
import com.mineswine.bridges.config.BridgesGameStorage;
import com.mineswine.bridges.config.BridgesMapConfig;
import com.mineswine.bridges.game.BridgesHandler;
import com.mineswine.bridges.game.BridgesMap;
import com.mineswine.bridges.kits.BridgesKit;
import com.mineswine.bridges.objects.BridgesPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by dylangellis on 1/23/15.
 */
public class Bridges extends JavaPlugin {

    private static LanguageFile languageFile;
    private static Bridges instance;

    public void onLoad() {
        this.deleteDir(new File("world"));
        this.copyDir(new File("worldcache"), new File("world"));
    }

    public void onEnable() {
        instance = this;
        GameAPI.init(new BridgesHandler(), new ObjectsInitializer() {
            public GameMap initMap(String name) {
                return new BridgesMap(name);
            }

            public GamePlayer initPlayer(Player player) {
                return new BridgesPlayer(player);
            }

            public GameTeam initTeam(TeamType type) {
                return new GameTeam(type) {
                };
            }

            public Kit initKit(String name) {
                return new BridgesKit(name);
            }
        }, new BridgesGameStorage(this, new BridgesGameConfig()), new BridgesMapConfig());
        Map<String, String> defaultMappings = new HashMap<String, String>();
        defaultMappings.put("game.claimedblock", "&cSomone else owns this block.");
        defaultMappings.put("game.claiminteract", "&cSomone else owns this block.");
        defaultMappings.put("game.voidplace", "&cYou cannot place over the void until bridges spawn.");
        defaultMappings.put("game.freerespawn", "&cYou have died, but now you get a free respawn. English.");
        defaultMappings.put("game.started", "&6The game has started.");
        defaultMappings.put("game.bridges", "&6Bridges have spawned.");
        defaultMappings.put("game.ended", "&cThe game has ended.");
        defaultMappings.put("game.name", "&cBridges");
        defaultMappings.put("game.maploading", "&6The map is loading...");
        defaultMappings.put("game.outoftime", "&cThere is no more time.");
        defaultMappings.put("game.teameliminated", "&eThe %team &eteam has been eliminated.");
        defaultMappings.put("item.wizard.firebook", "&cFire book");
        defaultMappings.put("item.wizard.lightningbook", "&cLightning book");
        defaultMappings.put("item.wizard.healingbook", "&cHealth book");
        defaultMappings.put("kit.scavenger.doubleresources", "&aYou got double resources!");
        defaultMappings.put("kit.enchanter.levelgained", "&aYou gained a level!");
        defaultMappings.put("kit.bomber.tntgained", "&cYou gained a bomb!");
        defaultMappings.put("kit.wizard.bookgained", "&cYou gained a %book book!");
        defaultMappings.put("scoreboard.bridges.format", LanguageFile.formatList(
                "&cRed %redplayers",
                "&aGreen %greenplayers",
                "&eYellow %yellowplayers",
                "&9Blue %blueplayers",
                "&6Bridge %bridgetime"
        ));
        defaultMappings.put("scoreboard.bridges.title", "&6Game Time: %gametime");
        for (GameMap map : GameAPI.getGameStorage().getMaps().values()) {
            defaultMappings.put("map." + map.getName() + ".display", map.getName());
        }
        languageFile = new LanguageFile(this, defaultMappings, Language.ENGLISH);
        languageFile.load();
        new CommandManager(this, new Class[]{CommandTest.class}).loadCommands();

        new BukkitRunnable() {
            public void run() {
                Bukkit.getWorld("world").setTime(6000);
            }
        }.runTaskTimer(this, 1, 1);
    }

    public static Bridges instance(){
        return instance;
    }

    public static BridgesGameStorage getStorage() {
        return ((BridgesGameStorage) GameAPI.getGameStorage());
    }

    public static BridgesGameConfig getGameConfig() {
        return (BridgesGameConfig) GameAPI.getGameStorage().getConfig();
    }

    public static LanguageFile getLanguageFile() {
        return languageFile;
    }

    public void deleteDir(File dir) {
        for (File f : dir.listFiles()) {
            System.out.println("Deleting " + f.getPath());
            if (f.isDirectory()) {
                deleteDir(f);
            }
            f.delete();
        }
        dir.delete();
    }

    public void copyDir(File from, File to) {
        to.mkdir();
        for (File f : from.listFiles()) {
            if (f.isDirectory()) {
                copyDir(f, new File(to, f.getName()));
            } else {
                try {
                    File ff = new File(to, f.getName());
                    ff.createNewFile();
                    InputStream fr = new FileInputStream(f);
                    OutputStream fw = new FileOutputStream(ff);
                    if (f.getName().contains("level")) {
                        fr = new GZIPInputStream(fr);
                        fw = new GZIPOutputStream(fw);
                    }

                    int r = 0;
                    while ((r = fr.read()) != -1) {
                        fw.write(r);
                    }
                    fw.flush();
                    fw.close();
                    fr.close();
                    System.out.println("Copied " + f.getPath() + " to " + ff.getPath());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
