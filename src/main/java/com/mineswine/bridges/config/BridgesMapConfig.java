package com.mineswine.bridges.config;

import com.mineswine.api.Backend;
import com.mineswine.api.gameapi.configuration.GameMapConfiguration;
import com.mineswine.bridges.game.BridgesMap;
import com.mineswine.bridges.objects.BridgePoint;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by dylangellis on 2/9/15.
 */
public class BridgesMapConfig extends GameMapConfiguration {


    public BridgesMapConfig(String... configArgs) {
        super("setorigin <map> - Sets the origin location of the map (where the map schematic will spawn)",
                "addbridge <map> <°> - Add a bridge to the map",
                "removebridge <map> <°> - Remove a bridge from the map depending on where you are facing.",
                "testbridge <map> <°> - Tests how a bridge will spawn (note this will add the bridge to the map, but will be undone 5 seconds afterwords)",
                "setteamspawn <map> <red/green/blue/yellow> - Sets the team spawn",
                "addmobspawn <map> - Add a mob spawn");
    }

    @Override
    public void run(final Player p, String arg, String... otherArgs) {
        if (arg.equals("setorigin")){
            ((BridgesMap)getMap(otherArgs[0])).setOrigin(p.getLocation().toVector());
            p.sendMessage("§aSet origin");
        } else if (arg.equals("addbridge")){
            ((BridgesMap) getMap(otherArgs[0])).addBridge(new BridgePoint(p.getLocation(), Integer.parseInt(otherArgs[1])));
            p.sendMessage("§aAdded bridge");
        } else if (arg.equals("removebridge")){
            ((BridgesMap) getMap(otherArgs[0])).removeBridge(new BridgePoint(p.getLocation(), Integer.parseInt(otherArgs[1])).getDirection());
            p.sendMessage("§cRemoved bridge.");
        } else if (arg.equals("testbridge")){
            final BridgePoint point = new BridgePoint(p.getLocation(), Integer.parseInt(otherArgs[1]));
            point.build(otherArgs[0]);
            p.sendMessage("§aBuilt");
            Bukkit.getScheduler().runTaskLater(Backend.instance(), new Runnable() {
                @Override
                public void run() {
                    p.sendMessage("§cRemoved");
                    point.getEditSession().undo(new EditSession(new BukkitWorld(Bukkit.getWorld("world")), Integer.MAX_VALUE));
                }
            }, 5 * 30);
        } else if (arg.equals("setteamspawn")){
            ((BridgesMap) getMap(otherArgs[0])).setTeamSpawn(otherArgs[1], p.getLocation().toVector());
            p.sendMessage("§aSet team spawn.");
        } else if (arg.equals("addmobspawn")) {
            ((BridgesMap) getMap(otherArgs[0])).addMobSpawn(p.getLocation().toVector());
            p.sendMessage("§aAdded mob spawn.");
        }
    }
}
