package com.mineswine.bridges.objects;

import com.mineswine.bridges.Bridges;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;

/**
 * Created by dylangellis on 2/4/15.
 */
public class BridgePoint {
    private Vector start;
    private int direction;
    private transient EditSession editSession;

    public BridgePoint(Location start, int direction) {
        this.start = start.toVector();
        this.direction = direction;
    }

    public void buildFancy(String map) {
        this.editSession = new EditSession(new BukkitWorld(Bukkit.getWorld("world")), Integer.MAX_VALUE);
        CuboidClipboard bridge = null;
        try {
            bridge = SchematicFormat.getFormat(new File(Bridges.instance().getDataFolder(), map + "_" + "bridge.schematic")).load(new File(Bridges.instance().getDataFolder(), map + "_" + "bridge.schematic"));
            bridge.rotate2D(direction);
            final com.sk89q.worldedit.Vector st = new com.sk89q.worldedit.Vector(start.getX(), start.getY(), start.getZ()).add(bridge.getOffset());
            boolean xneg = Math.cos(Math.toRadians(direction)) < 0, zneg = Math.sin(Math.toRadians(direction)) < 0;
            int i = 0, x = 0, z = 0;
            for (int nm = 0; (bridge.getSize().getBlockX() > bridge.getSize().getBlockZ() ? x < bridge.getSize().getBlockX() : z < bridge.getSize().getBlockZ()); nm++) {
                for (int y = 0; y < bridge.getSize().getBlockY(); ++y) {
                    for (int nm3 = 0; (bridge.getSize().getBlockX() > bridge.getSize().getBlockZ() ? z < bridge.getSize().getBlockZ() : x < bridge.getSize().getBlockX()); ++nm3) {
//                       Bukkit.broadcastMessage("XROFL "+x+" "+y+" "+z);
                        final BaseBlock block = bridge.getBlock(new com.sk89q.worldedit.Vector(x, y, z));
                        if (block == null) {
                            if (bridge.getSize().getBlockX() > bridge.getSize().getBlockZ())
                                z++;
                            else
                                x++;
                            continue;
                        }
                        if (block.isAir()) {
                            if (bridge.getSize().getBlockX() > bridge.getSize().getBlockZ())
                                z++;
                            else
                                x++;
                            continue;
                        }
                        final int finalX = x;
                        final int finalY = y;
                        final int finalZ = z;
                        final CuboidClipboard finalBridge = bridge;
                        Bukkit.getScheduler().runTaskLater(Bridges.instance(), new Runnable() {
                            public void run() {
                                try {
//                                    Bukkit.broadcastMessage("NIG54A "+finalX+" "+finalY+" "+finalZ);
                                    com.sk89q.worldedit.Vector vector = new com.sk89q.worldedit.Vector(finalX, finalY, finalZ).add(st);
                                    Bukkit.getWorld("world").playEffect(new Location(Bukkit.getWorld("world"), vector.getX(), vector.getY(), vector.getZ()), Effect.STEP_SOUND, block.getType());
                                    editSession.setBlock(vector, block);
                                } catch (MaxChangedBlocksException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, (direction == 90 || direction == 180 ? (bridge.getSize().getBlockX() > bridge.getSize().getBlockZ() ? bridge.getSize().getBlockX() : bridge.getSize().getBlockZ()) - i : i) * 20);
                        if (bridge.getSize().getBlockX() > bridge.getSize().getBlockZ())
                            z++;
                        else
                            x++;
                    }
                    if (bridge.getSize().getBlockX() > bridge.getSize().getBlockZ())
                        z = 0;
                    else
                        x = 0;
                }
                i++;
                if (bridge.getSize().getBlockX() > bridge.getSize().getBlockZ())
                    x++;
                else
                    z++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DataException e) {
            e.printStackTrace();
        }
        /*
          for (int x = 0; x < size.getBlockX(); ++x) {
            for (int y = 0; y < size.getBlockY(); ++y) {
                for (int z = 0; z < size.getBlockZ(); ++z) {
                    final BaseBlock block = data[x][y][z];
                    if (block == null) {
                        continue;
                    }

                    if (noAir && block.isAir()) {
                        continue;
                    }

                    editSession.setBlock(new Vector(x, y, z).add(newOrigin), block);
                }
            }
        }
         */
    }

    public void build(String map) {
        try {
            this.editSession = new EditSession(new BukkitWorld(Bukkit.getWorld("world")), Integer.MAX_VALUE);
            CuboidClipboard bridge = SchematicFormat.getFormat(new File(Bridges.instance().getDataFolder(), map + "_" + "bridge.schematic")).load(new File(Bridges.instance().getDataFolder(), map + "_" + "bridge.schematic"));
            bridge.rotate2D(direction);
            bridge.paste(editSession, new com.sk89q.worldedit.Vector(start.getX(), start.getY(), start.getZ()), false);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DataException e) {
            e.printStackTrace();
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }

    public Vector getStart() {
        return start;
    }

    public int getDirection() {
        return direction;
    }

    public EditSession getEditSession() {
        return editSession;
    }
}
