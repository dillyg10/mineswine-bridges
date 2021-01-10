package com.mineswine.bridges.game;

import com.mineswine.api.gameapi.GameAPI;
import com.mineswine.api.gameapi.gamehandler.GameHandler.DefaultGamePhase;
import com.mineswine.api.gameapi.gamehandler.GameTeam;
import com.mineswine.api.gameapi.gamehandler.GameTeam.TeamType;
import com.mineswine.api.gameapi.gamehandler.TeamHandler;
import com.mineswine.api.gameapi.gamemap.GameMap;
import com.mineswine.api.gameapi.language.Language;
import com.mineswine.api.gameapi.objects.GamePlayer;
import com.mineswine.api.gameapi.objects.KeyValuePair;
import com.mineswine.api.gameapi.objects.Returnable;
import com.mineswine.api.gameapi.ui.ScoreboardUI;
import com.mineswine.api.util.TimeUtils;
import com.mineswine.bridges.Bridges;
import com.mineswine.bridges.config.BridgesGameConfig.OreData;
import com.mineswine.bridges.kits.BridgesKitManager;
import com.mineswine.bridges.objects.BridgePoint;
import com.mineswine.bridges.objects.BridgesPlayer;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by dylangellis on 1/23/15.
 */
public class BridgesMap extends GameMap {
    private Vector origin = new Vector();
    private List<BridgePoint> bridges = new ArrayList<BridgePoint>();
    private Map<String, Vector> teamSpawns = new HashMap<String, Vector>();
    private List<Vector> mobSpawns = new ArrayList<Vector>();
    private transient GamePlayer second, third;
    private transient CuboidClipboard mapSchematic;
    private transient BridgesKitManager manager;
    private transient ScoreboardUI scoreboard;
    private transient GridValue[][][] grid;
    private transient Random random = new Random();
    private transient ArrayList<GameTeam> eliminated = new ArrayList<GameTeam>();
    private transient int spawnTime = 0;
    public static transient final EntityType[] entities = new EntityType[]{EntityType.COW, EntityType.CHICKEN, EntityType.PIG};
    public BridgesMap(String name) {
        super(name);
    }


    @Override
    public void initted() {
        second = null;
        third = null;

        random = new Random();
        eliminated = new ArrayList<GameTeam>();
        if (mobSpawns == null)
            mobSpawns = new ArrayList<Vector>();
        spawnTime = random.nextInt(Bridges.getGameConfig().getMaxMobSpawnTime() - Bridges.getGameConfig().getMinMobSpawnTime()) + Bridges.getGameConfig().getMinMobSpawnTime();
        this.mapSchematic = getSchematic();
        this.manager = new BridgesKitManager();
        scoreboard = new ScoreboardUI("bridges", Bridges.getLanguageFile(),
                new KeyValuePair<Returnable<?, GamePlayer>>("%bridgetime", new Returnable<String, GamePlayer>() {
                    public String returnValue(GamePlayer param) {
                        return getGameTime() > Bridges.getGameConfig().getBridgeTime() ? null : TimeUtils.formatTime(Bridges.getGameConfig().getBridgeTime() - getGameTime());
                    }
                }), new KeyValuePair<Returnable<?, GamePlayer>>("%redplayers", new Returnable<Integer, GamePlayer>() {
            public Integer returnValue(GamePlayer param) {
                return TeamHandler.getTeam(TeamType.RED).getSize() == 0 ? null : TeamHandler.getTeam(TeamType.RED).getSize();
            }
        }), new KeyValuePair<Returnable<?, GamePlayer>>("%blueplayers", new Returnable<Integer, GamePlayer>() {
            public Integer returnValue(GamePlayer param) {
                return TeamHandler.getTeam(TeamType.BLUE).getSize() == 0 ? null : TeamHandler.getTeam(TeamType.BLUE).getSize();
            }
        }), new KeyValuePair<Returnable<?, GamePlayer>>("%greenplayers", new Returnable<Integer, GamePlayer>() {
            public Integer returnValue(GamePlayer param) {
                return TeamHandler.getTeam(TeamType.GREEN).getSize() == 0 ? null : TeamHandler.getTeam(TeamType.GREEN).getSize();
            }
        }), new KeyValuePair<Returnable<?, GamePlayer>>("%yellowplayers", new Returnable<Integer, GamePlayer>() {
            public Integer returnValue(GamePlayer param) {
                return TeamHandler.getTeam(TeamType.YELLOW).getSize() == 0 ? null : TeamHandler.getTeam(TeamType.YELLOW).getSize();
            }
        }), new KeyValuePair<Returnable<?, GamePlayer>>("%bluelinecheck", new Returnable<String, GamePlayer>() {
            public String returnValue(GamePlayer param) {
                return TeamHandler.getTeam(TeamType.BLUE).getSize() == 0 ? null : "&c";
            }
        }), new KeyValuePair<Returnable<?, GamePlayer>>("%redlinecheck", new Returnable<String, GamePlayer>() {
            public String returnValue(GamePlayer param) {
                return TeamHandler.getTeam(TeamType.RED).getSize() == 0 ? null : "&c";
            }
        }), new KeyValuePair<Returnable<?, GamePlayer>>("%yellowlinecheck", new Returnable<String, GamePlayer>() {
            public String returnValue(GamePlayer param) {
                return TeamHandler.getTeam(TeamType.YELLOW).getSize() == 0 ? null : "&c";
            }
        }), new KeyValuePair<Returnable<?, GamePlayer>>("%greenlinecheck", new Returnable<String, GamePlayer>() {
            public String returnValue(GamePlayer param) {
                return TeamHandler.getTeam(TeamType.GREEN).getSize() == 0 ? null : "&c";
            }
        }), new KeyValuePair<Returnable<?, GamePlayer>>("%map", new Returnable<String, GamePlayer>() {
            public String returnValue(GamePlayer param) {
                return getDisplayName(param.getLanguage());
            }
        }), new KeyValuePair<Returnable<?, GamePlayer>>("%bridgelinecheck", new Returnable<String, GamePlayer>() {
            public String returnValue(GamePlayer param) {
                return getGameTime() > Bridges.getGameConfig().getBridgeTime() ? null : "&c";
            }
        }));
        scoreboard.updateTitle("scoreboard.bridges.title", new KeyValuePair("%gametime", TimeUtils.formatTime(Bridges.getGameConfig().getGameTime() - getGameTime())), new KeyValuePair("%map", this));
        Bukkit.getScheduler().runTaskLater(Bridges.instance(), new Runnable() {
            public void run() {
                spawnPlayers();
                for (GamePlayer player : getPlayers()) {
                    player.freeze();
                }
            }
        }, 5);
    }

    public boolean checkWin() {
        GameTeam winner = null;
        if (GameAPI.getHandler().inPhase(DefaultGamePhase.LOBBY) || GameAPI.getHandler().inPhase(DefaultGamePhase.VOTING))
            return false;
        for (GameTeam team : TeamHandler.getTeams()) {
            if (team.getSize() == 0 && !eliminated.contains(team)) {
                eliminated.add(team);
                GameAPI.broadcast(Bridges.getLanguageFile(), "game.teameliminated", new KeyValuePair("%team", team));
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p.getLocation(), Sound.WITHER_SPAWN, 1.0f, 1.0f);
                }
            }
        }
        for (GameTeam team : TeamHandler.getTeams()) {
            if (winner != null && team.getSize() > 0)
                return false;
            if (team.getSize() > 0)
                winner = team;
        }
        GameAPI.broadcast(Bridges.getLanguageFile(), "game.ended");
        declareWinner(getPlayers().get(0), second == null ? getPlayers().size() >= 2 ? getPlayers().get(1) : null : second, third, Bridges.getLanguageFile());
        GameAPI.getHandler().end();
        return true;
    }


    public void playerDied(GamePlayer player) {
        if (getHandler().inPhase(BRIDGES)) {
            super.playerDied(player);
        } else if (((BridgesPlayer) player).hasDiedBefore()) {
            super.playerDied(player);
        } else {
            ((BridgesPlayer) player).setDiedBefore(true);
            player.getPlayer().teleport(teamSpawns.get(player.getTeam().getType().toString()).toLocation(Bukkit.getWorld("world")).clone().add(0, 1, 0));
            player.sendLangMessage(Bridges.getLanguageFile(), "game.freerespawn");
            player.equiptKit();
        }
    }

    @Override
    public void playerAdded(final GamePlayer player) {
        scoreboard.addPlayer(player);
        Bukkit.getScheduler().runTaskLater(Bridges.instance(), new Runnable() {
            public void run() {
                player.setFrozen(false);
            }
        }, 4);
    }

    @Override
    public void playerRemoved(GamePlayer player) {
        scoreboard.removePlayer(player);
        checkWin();
    }

    public void removePlayer(GamePlayer player) {
        if (isEnding())
            return;
        if (getHandler().inPhase(DefaultGamePhase.IN_GAME)) {
            checkSecondThird(player);
        }
        super.removePlayer(player);
    }

    @Override
    public void playerSpectated(GamePlayer player) {
        if (isEnding())
            return;
        checkWin();
        if (player.getLastScoreboard() != null && player.getLastScoreboard().equals(scoreboard))
            return;
        scoreboard.addPlayer(player);
    }

    public void spectatePlayer(GamePlayer player) {
        checkSecondThird(player);
        super.spectatePlayer(player);
    }

    @Override
    public void started() {
        manager.register();
        drawGrid();
        generateOres();
        for (GamePlayer player : getPlayers()){
            manager.applyKitAbility(player);
            player.unfreeze();
        }
        for (Vector spawn : mobSpawns) {
            for (int i = 0; i < new Random().nextInt(3) + 1; i++) {
                Bukkit.getWorld("world").spawnEntity(spawn.toLocation(Bukkit.getWorld("world")).clone().add(random.nextGaussian() * 1.2, 0, random.nextGaussian() * 1.2), entities[random.nextInt(entities.length)]);
            }
        }
        GameAPI.broadcast(Bridges.getLanguageFile(), "game.started");
    }

    @Override
    public void ended() {
        for (GamePlayer player : GameAPI.getHandler().getPlayers()) {
            manager.fixKitAbility(player);
        }
        manager.unregister();
    }

    public void lateEnd() {
        super.lateEnd();
        for (int x = 0; x < mapSchematic.getSize().getBlockX(); x++) {
            for (int y = 0; y < mapSchematic.getSize().getBlockY(); y++) {
                for (int z = 0; z < mapSchematic.getSize().getBlockZ(); z++) {
                    origin.clone().add(new Vector(mapSchematic.getOffset().getX(), mapSchematic.getOffset().getY(), mapSchematic.getOffset().getZ())).add(new Vector(x, y, z)).toLocation(Bukkit.getWorld("world")).getBlock().setType(Material.AIR, false);
                }
            }
        }
        for (GamePlayer player : getPlayers()) {
            ((BridgesPlayer) player).setDiedBefore(false);
        }
    }

    public void bridges(){
        for (BridgePoint point : bridges){
            point.buildFancy(getName());
        }
        getHandler().beginPhase(BRIDGES);
        GameAPI.broadcast(Bridges.getLanguageFile(), "game.bridges");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0f, 1.0f);
        }
    }

    @Override
    public void gameTick() {
        scoreboard.updateTitle("scoreboard.bridges.title", new KeyValuePair("%gametime", TimeUtils.formatTime(Bridges.getGameConfig().getGameTime() - getGameTime())), new KeyValuePair("%map", this));
        scoreboard.update();
        if (spawnTime-- == 0) {
            Collections.shuffle(mobSpawns);
            for (int i = 0; i < new Random().nextInt(3) + 1; i++) {
                Bukkit.getWorld("world").spawnEntity(mobSpawns.get(0).toLocation(Bukkit.getWorld("world")), entities[random.nextInt(entities.length)]);
            }
            spawnTime = random.nextInt(Bridges.getGameConfig().getMaxMobSpawnTime() - Bridges.getGameConfig().getMinMobSpawnTime()) + Bridges.getGameConfig().getMinMobSpawnTime();
        }
        if (!getHandler().inPhase(BRIDGES) && getGameTime() == Bridges.getGameConfig().getBridgeTime()){
            bridges();
        }
        if (getGameTime() == Bridges.getGameConfig().getGameTime()) {
            GameAPI.broadcast(Bridges.getLanguageFile(), "game.outoftime");
            GameAPI.getHandler().end();
        }
    }

    @Override
    public boolean spectateIfJoinLate() {
        return true;
    }

    @Override
    public String getDisplayName(Language type) {
        return Bridges.getLanguageFile().get("map." + getName() + ".display", type);
    }

    public static final int BRIDGES = 8;

    public CuboidClipboard getSchematic(){
        try {
            return SchematicFormat.getFormat(new File(Bridges.instance().getDataFolder(), getName() + ".schematic")).load(new File(Bridges.instance().getDataFolder(), getName() + ".schematic"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DataException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void checkSecondThird(GamePlayer player) {
        if (getPlayers().size() == 2) {
            second = player;
        }
        if (getPlayers().size() == 3) {
            third = player;
        }
    }

    public void generateOres() {
        new BukkitRunnable() {
            int gx;
            public void run() {
                int regionsDone = 0;
                for (int gx = this.gx; gx < grid.length; gx++) {
                    for (int gy = 0; gy < grid[0].length; gy++) {
                        for (int gz = 0; gz < grid[0][0].length; gz++) {
                            GridValue val = grid[gx][gy][gz];
                            if (val == null)
                                continue;
                            int x = val.x, y = val.y, z = val.z;
                            Location l = new Vector(x, y, z).toLocation(Bukkit.getWorld("world"));
//                            l.getBlock().setType(Material.EMERALD_BLOCK);
                            //							l.getBlock().setType(Material.GOLD_BLOCK);

                            OreData dat = null;
                            mainloop:
                            for (OreData d : Bridges.getGameConfig().getOres()) {
                                if (y > d.depth)
                                    continue;
                                for (int dx = -d.veinDistance; dx <= d.veinDistance; dx++) {
                                    for (int dz = -d.veinDistance; dz <= d.veinDistance; dz++) {
                                        for (int dy = -d.veinDistance; dy <= d.veinDistance; dy++) {
                                            GridValue g = grid[Math.max(0, Math.min(grid.length - 1, dx + gx))][Math.max(0, Math.min(grid[0].length - 1, dy + gy))][Math.max(0, Math.min(grid[0][0].length - 1, dz + gz))];
                                            if (g == null)
                                                continue;
                                            if (g.material == null)
                                                continue;
                                            if (g.material.getId() == d.__id__)
                                                continue mainloop;
                                        }
                                    }
                                }
                                if (random.nextInt(100) < d.percentSpawn) {
                                    dat = d;
                                    break;
                                }
                            }
                            if (dat == null)
                                continue;
                            regionsDone++;
                            Location n = l.clone();
                            val.material = Material.getMaterial(dat.__id__);
                            n = n.add(random.nextInt(2) * (random.nextBoolean() ? -1 : 1), random.nextInt(2) * (random.nextBoolean() ? -1 : 1), random.nextInt(2) * (random.nextBoolean() ? -1 : 1));
                            int oreSize = random.nextInt(dat.maxVein - dat.minVein) + dat.minVein;
                            for (int i = 0; i < oreSize; i++) {
                                int tries = 0;
                                while (true) {
                                    if (tries == 50)
                                        break;
                                    Location n2 = n.clone().add(random.nextInt(3), random.nextInt(3), random.nextInt(3));
//                                    if (n2.getBlockY() > origin.getBlockY() + mapSchematic.getSize().getBlockY())
//                                        continue;
                                    if (n2.getBlock().getTypeId() != Material.STONE.getId()) {
                                        tries++;
                                        continue;
                                    }
                                    n2.getBlock().setType(Material.getMaterial(dat.__id__));
                                    break;
                                }
                            }
                        }
                    }
//                    if (regionsDone >= 12 * 12 * 12) {
//                        this.gx = gx;
//                        return;
//                    }
                }
                cancel();
            }
        }.runTaskTimer(Bridges.instance(), 0, 5);
    }

    public void drawGrid() {
        int gx = 0, gy = 0, gz = 0;
        Vector corner1 = origin.clone().add(new Vector(mapSchematic.getOffset().getX(), mapSchematic.getOffset().getY(), mapSchematic.getOffset().getZ()));
        Vector corner2 = corner1.clone().add(new Vector(mapSchematic.getSize().getX(), mapSchematic.getSize().getY(), mapSchematic.getSize().getZ()));
        grid = new GridValue[((corner2.getBlockX() - corner1.getBlockX()) / 3) + 1 + (((corner2.getBlockX() - corner1.getBlockX()) % 3) == 0 ? 0 : 1)][((corner2.getBlockY() - corner1.getBlockY()) / 3) + 1 + (((corner2.getBlockY() - corner1.getBlockY()) % 3) == 0 ? 0 : 1)][((corner2.getBlockZ() - corner1.getBlockZ()) / 3) + 1 + (((corner2.getBlockZ() - corner1.getBlockZ()) % 3) == 0 ? 0 : 1)];
        for (int x = corner1.getBlockX(); x <= corner2.getBlockX(); x += 3) {
            for (int y = corner1.getBlockY(); y <= corner2.getBlockY(); y += 3) {
                outerloop:
                for (int z = corner1.getBlockZ(); z <= corner2.getBlockZ(); z += 3) {
                    for (int x2 = x; x2 <= x + 3; x2++) {
                        for (int y2 = y; y2 <= y + 3; y2++) {
                            for (int z2 = z; z2 <= z + 3; z2++) {
                                if (new Vector(x2, y2, z2).toLocation(Bukkit.getWorld("world")).getBlock().getType() == Material.STONE) {
                                    grid[gx][gy][gz] = new GridValue(x, y, z);
                                    gz++;
                                    continue outerloop;
                                }
                            }
                        }
                    }
                    gz++;
                }
                gy++;
                gz = 0;
            }
            gx++;
            gy = 0;
        }
    }

    public class GridValue {
        public int x;
        public int y;
        public int z;
        public Material material = Material.AIR;

        public GridValue(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public void spawnPlayers() {
        for (GamePlayer player : getPlayers()) {
            player.getPlayer().teleport(teamSpawns.get(player.getTeam().getType().toString()).clone().add(new Vector(random.nextGaussian() * 3, 1, random.nextGaussian() * 3)).toLocation(Bukkit.getWorld("world")).clone());
            player.freeze();
        }
    }


    public void addBridge(BridgePoint point){
        this.bridges.add(point);
    }

    public void addMobSpawn(Vector spawn) {
        mobSpawns.add(spawn);
    }

    public void removeBridge(int dirrection){
        for (BridgePoint point : bridges){
            if (point.getDirection() == dirrection) {
                bridges.remove(point);
                break;
            }
        }
    }

    public void setTeamSpawn(String team, Vector spawn){
        teamSpawns.put(team.toUpperCase(),spawn);
    }

    public void setOrigin(Vector origin) {
        this.origin = origin;
    }
}
