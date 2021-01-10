package com.mineswine.bridges.kits;

import com.mineswine.api.Backend;
import com.mineswine.api.gameapi.AbilityManager;
import com.mineswine.api.gameapi.GameAPI;
import com.mineswine.api.gameapi.gamehandler.GameHandler.DefaultGamePhase;
import com.mineswine.api.gameapi.loot.LootChest;
import com.mineswine.api.gameapi.objects.GamePlayer;
import com.mineswine.api.gameapi.objects.KeyValuePair;
import com.mineswine.api.util.Animations;
import com.mineswine.api.util.Animations.AnimationType;
import com.mineswine.api.util.ItemStackUtil;
import com.mineswine.bridges.Bridges;
import com.mineswine.bridges.config.BridgesGameConfig.BridgesKits.acrobat;
import com.mineswine.bridges.config.BridgesGameConfig.BridgesKits.archer;
import com.mineswine.bridges.config.BridgesGameConfig.BridgesKits.baker;
import com.mineswine.bridges.config.BridgesGameConfig.BridgesKits.bomber;
import com.mineswine.bridges.config.BridgesGameConfig.BridgesKits.enchanter;
import com.mineswine.bridges.config.BridgesGameConfig.BridgesKits.scavenger;
import com.mineswine.bridges.config.BridgesGameConfig.BridgesKits.wizard;
import com.mineswine.bridges.game.BridgesMap;
import com.mineswine.bridges.objects.BridgesPlayer;
import net.minecraft.server.v1_8_R2.Block;
import net.minecraft.server.v1_8_R2.BlockPosition;
import net.minecraft.server.v1_8_R2.Blocks;
import net.minecraft.server.v1_8_R2.EntityArrow;
import net.minecraft.server.v1_8_R2.World;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftArrow;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R2.util.CraftMagicNumbers;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by dylangellis on 2/5/15.
 */
public class BridgesKitManager extends AbilityManager {
      /*
    Baker: Earn free cookies overtime,  Left-click cookie to throw them at your enemies and knock them back
    Scavenger: Gain resources faster (30% chance to get double resources)
    Enchanter: gain free enchantment levels overtime
    Wizard: You get a free spell book every 2 minutes. Maximum of 2 can be stored up. \
    - Fire Spell: Shoots a circular blast of the mob cage fire effect 10 blocks in front of you lighting everyone on fire.
    - Lightning Spell: Calls down a lightning bolt at the target location.
    - Healing Spell: Heals you  for 2 hearts.
    Bomber: You get 1 TNT every 2 minutes. Maximum of 2. When you place the TNT is primed.
    Acrobat: double jump
    Archer: Earn free arrows overtime, Your arrows pierce through blocks (except furnances,chests,workbenches)
    Boxer: Damaging your enemies gives them slow II for 3 seconds.
     */

    @EventHandler(priority = EventPriority.HIGHEST)
    public void place(final BlockPlaceEvent event) {
        voidcheck:
        if (!GameAPI.getHandler().inPhase(BridgesMap.BRIDGES)) {
            for (int i = event.getBlock().getY() - 1; i >= 0; i--) {
                if (new Location(event.getBlock().getWorld(), event.getBlock().getLocation().getBlockX(), i, event.getBlock().getLocation().getBlockZ()).getBlock().getType() != Material.AIR)
                    break voidcheck;
            }
            GamePlayer.getPlayer(event.getPlayer()).sendLangMessage(Bridges.getLanguageFile(), "game.voidplace");
            event.setCancelled(true);
            return;
        }
        if (event.getBlockPlaced().getType() == Material.CHEST){
            Bukkit.getScheduler().runTaskLater(Backend.instance(), new Runnable() {
                @Override
                public void run() {
                    LootChest.getChest((Chest)event.getBlockPlaced().getState()).setLooted();
                }
            }, 1);
        }
        for (String type : Bridges.getGameConfig().getClaimBlocks()) {
            if (type.equals(event.getBlockPlaced().getType().toString())) {
                BridgesPlayer player = ((BridgesPlayer) GamePlayer.getPlayer(event.getPlayer()));
                if (player.getClaimed().size() > 4) {
                    player.getClaimed().get(0).getBlock().removeMetadata("owned", Backend.instance());
                    player.getClaimed().remove(0);
                }
                event.getBlock().setMetadata("owned", new FixedMetadataValue(Backend.instance(), event.getPlayer().getName()));
                player.getClaimed().add(event.getBlockPlaced().getLocation());
            }
        }
        if (event.getBlockPlaced().getType() == Material.TNT) {
            event.getBlockPlaced().setType(Material.AIR);
            event.getPlayer().getWorld().spawn(event.getBlock().getLocation(), TNTPrimed.class);
            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.FUSE, 1.0f, 1.0f);
        }
        event.setCancelled(false);
    }

    @EventHandler
    public void chat(AsyncPlayerChatEvent event) {
        if (GameAPI.getHandler().inPhase(DefaultGamePhase.IN_GAME)) {
            GamePlayer player = GamePlayer.getPlayer(event.getPlayer());
            if (!player.isSpectator()) {
                event.setCancelled(true);
                for (GamePlayer player1 : GameAPI.getHandler().getMap().getPlayers()) {
                    if (player1.getTeam().equals(player.getTeam())) {
                        player1.getPlayer().sendMessage(event.getPlayer().getDisplayName() + "§7: " + event.getMessage());
                    }
                }
            }
        }
    }


    @EventHandler
    public void interact(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getClickedBlock().hasMetadata("owned")) {
            String name = event.getClickedBlock().getMetadata("owned").get(0).asString();

            if (name != null && !name.equals(event.getPlayer().getName()) && Bukkit.getPlayer(name) != null && GamePlayer.getPlayer(Bukkit.getPlayer(name)).getTeam().equals(GamePlayer.getPlayer(event.getPlayer()).getTeam())) {
                event.setCancelled(true);
                GamePlayer.getPlayer(event.getPlayer()).sendLangMessage(Bridges.getLanguageFile(), "game.claimedblock");
                return;
            }
        }
        if (event.getItem() != null && event.getItem().getItemMeta().hasDisplayName()) {
            GamePlayer player = GamePlayer.getPlayer(event.getPlayer());
            String name = event.getItem().getItemMeta().getDisplayName();
            if (name.equals(Bridges.getLanguageFile().get("item.wizard.firebook", player.getLanguage()))) {
                Fireball fireball = player.getPlayer().launchProjectile(Fireball.class);
                fireball.setVelocity(fireball.getVelocity().multiply(get(wizard.class).fireballVelocity));
                event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.GHAST_FIREBALL, 1.0f, 1.0f);
            } else if (name.equals(Bridges.getLanguageFile().get("item.wizard.lightningbook", player.getLanguage()))) {
                player.getPlayer().getWorld().strikeLightning(player.getPlayer().getTargetBlock((HashSet<Byte>) null, get(wizard.class).lightningRange).getLocation());
            } else if (name.equals(Bridges.getLanguageFile().get("item.wizard.healingbook", player.getLanguage()))) {
                player.getPlayer().setHealth(Math.min(player.getPlayer().getMaxHealth(), player.getPlayer().getHealth() + 2));
                player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ZOMBIE_UNFECT, 1.0f, 1.0f);
                Animations.animate(Animations.constructPacket(AnimationType.HEART, player.getPlayer().getLocation().add(0, 1, 0), new Vector(new Random().nextGaussian() % 0.4, new Random().nextGaussian() % 0.4, new Random().nextGaussian() % 0.4), 0.003f, 10, ""));
            } else
                return;
            dih(player.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void blockBreak(final BlockBreakEvent event) {
        if (event.getBlock().hasMetadata("owned")) {
            String name = event.getBlock().getMetadata("owned").get(0).asString();
            if (!name.equals(event.getPlayer().getName()) && Bukkit.getPlayer(name) != null && GamePlayer.getPlayer(Bukkit.getPlayer(name)).getTeam().equals(GamePlayer.getPlayer(event.getPlayer()))) {
                event.setCancelled(true);
                GamePlayer.getPlayer(event.getPlayer()).sendLangMessage(Bridges.getLanguageFile(), "game.claimedblock");
                return;
            }
        }

        final GamePlayer player = GamePlayer.getPlayer(event.getPlayer());
        if (event.getBlock().getType() == Material.GRAVEL && new Random().nextInt(100) <= 30 && !player.isSpectator()) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.BONE));
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.GRAVEL));
            return;
        }
        if (hasKit(player, BridgesKits.SCAVENGER) && new Random().nextInt(100) < get(scavenger.class).resourcePercent) {
            event.setCancelled(true);
            player.sendLangMessage(Bridges.getLanguageFile(), "kit.scavenger.doubleresources");
            World world = ((CraftWorld) player.getPlayer().getWorld()).getHandle();
            Block block = CraftMagicNumbers.getBlock(event.getBlock());
            try {
                net.minecraft.server.v1_8_R2.ItemStack drop = new net.minecraft.server.v1_8_R2.ItemStack(block.getDropType(block.getBlockData(), new Random(), 0), block.getDropCount(1, new Random()) * 2);
                world.getWorld().dropItem(event.getBlock().getLocation(), CraftItemStack.asBukkitCopy(drop));
            } catch (Exception ex) {
                world.getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(event.getBlock().getType() == Material.STONE ? Material.COBBLESTONE : event.getBlock().getType(), 2));
            }
            event.getBlock().setType(Material.AIR);
            return;
        }
        event.setCancelled(player.isSpectator());
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void creatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(false);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void chunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (!(entity instanceof Player)) {
                entity.remove();
            }
        }
    }


    @EventHandler
    public void toggleFlight(final PlayerToggleFlightEvent event) {
        if (hasKit(GamePlayer.getPlayer(event.getPlayer()), BridgesKits.ACROBAT)) {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
                return;
            if (event.getPlayer().hasMetadata("doublejump")) {
                event.setCancelled(true);
                event.getPlayer().setAllowFlight(false);
                return;
            }
            event.setCancelled(true);
            event.getPlayer().setVelocity(event.getPlayer().getLocation().getDirection().multiply(get(acrobat.class).doubleJumpVelocityMultiplier).setY(get(acrobat.class).doubleJumpVelocityY));
            event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ENDERDRAGON_WINGS, 1.0f, 1.0f);
            Animations.animate(Animations.constructPacket(AnimationType.CLOUD, event.getPlayer().getLocation(), 0.03f, 5, ""));
            event.getPlayer().setMetadata("doublejump", new FixedMetadataValue(Bridges.instance(), true));
            Bukkit.getScheduler().runTaskLater(Backend.instance(), new Runnable() {
                public void run() {
                    if (event.getPlayer().isOnline()) {
                        event.getPlayer().removeMetadata("doublejump", Bridges.instance());
                        event.getPlayer().setAllowFlight(true);
                    }
                }
            }, get(acrobat.class).doubleJumpTickTime);
        }
    }

    @EventHandler
    public void damageEvent(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player && hasKit(GamePlayer.getPlayer((Player) event.getDamager()), BridgesKits.BOXER)) {
            ((Player) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 0), true);
        }
    }

    @EventHandler
    public void projectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow) {
            final Arrow arrow = (Arrow) event.getEntity();
            if (arrow.getShooter() instanceof Player && hasKit(GamePlayer.getPlayer((Player) arrow.getShooter()), BridgesKits.ARCHER)) {
                Bukkit.getScheduler().runTaskLater(Bridges.instance(), new Runnable() {
                    public void run() {
                        try {
                            World world = ((CraftWorld) arrow.getWorld()).getHandle();
                            EntityArrow entityArrow = ((CraftArrow) arrow).getHandle();
                            Field g = entityArrow.getClass().getDeclaredField("g");
                            g.setAccessible(true);
                            Block block = (Block) g.get(entityArrow);
                            if (block == null || block == Blocks.CHEST || block == Blocks.FURNACE || block == Blocks.CRAFTING_TABLE)
                                return;
                            Field d = entityArrow.getClass().getDeclaredField("d");
                            Field e = entityArrow.getClass().getDeclaredField("e");
                            Field f = entityArrow.getClass().getDeclaredField("f");
                            d.setAccessible(true);
                            e.setAccessible(true);
                            f.setAccessible(true);
                            Vector location = new Vector(d.getInt(entityArrow), e.getInt(entityArrow), f.getInt(entityArrow));
                            BlockPosition position = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                            block.dropNaturally(world, position, block.getBlockData(), 1.0f, 1);
                            world.setAir(position);
                            entityArrow.die();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }, 1L);

            }
        }
    }

    public void tick(int tick) {
        for (GamePlayer player : GameAPI.getHandler().getPlayers()) {
            if (tick % get(baker.class).cookieTime == 0 && hasKit(player, BridgesKits.BAKER)) {
                player.getPlayer().getInventory().addItem(new ItemStack(Material.COOKIE));
            } else if (tick % get(enchanter.class).levelGainTime == 0 && hasKit(player, BridgesKits.ENCHANTER)) {
                player.getPlayer().setLevel(player.getPlayer().getLevel() + 1);
                player.sendLangMessage(Bridges.getLanguageFile(), "kit.enchanter.levelgained");
            } else if (tick % get(wizard.class).wizardBookTime == 0 && hasKit(player, BridgesKits.WIZARD)) {
                if (ItemStackUtil.countItems(player.getPlayer(), Material.ENCHANTED_BOOK) < get(wizard.class).max) {
                    String name = Bridges.getLanguageFile().get(get(wizard.class).books[new Random().nextInt(get(wizard.class).books.length)], player.getLanguage());
                    player.getPlayer().getInventory().addItem(ItemStackUtil.create(Material.ENCHANTED_BOOK, 1, 0, name));
                    player.sendLangMessage(Bridges.getLanguageFile(), "kit.wizard.bookgained", new KeyValuePair("%book", name));
                }
            } else if (tick % get(bomber.class).tntTime == 0 && hasKit(player, BridgesKits.BOMBER)) {
                if (ItemStackUtil.countItems(player.getPlayer(), Material.TNT) < get(bomber.class).max) {
                    player.getPlayer().getInventory().addItem(new ItemStack(Material.TNT));
                    player.sendLangMessage(Bridges.getLanguageFile(), "kit.bomber.tntgained");
                }
            } else if (tick % get(archer.class).arrowTime == 0 && hasKit(player, BridgesKits.ARCHER)) {
                player.getPlayer().getInventory().addItem(new ItemStack(Material.ARROW));
            }
        }
    }

    public void secondTick() {

    }

    public void applyKitAbility(GamePlayer player) {
        if (hasKit(player, BridgesKits.ACROBAT)) {
            player.getPlayer().setAllowFlight(true);
        }
    }

    public boolean hasKit(GamePlayer player, BridgesKits kits) {
        return hasAbility(player, kits.ordinal());
    }

    private <T> T get(Class<T> clazz) {
        return Bridges.getGameConfig().getKitConfig(clazz);
    }


    public static void dih(final Player p) {
        final ItemStack hand = p.getItemInHand();
        if (hand.getAmount() <= 1) {
            hand.setType(Material.AIR);
        } else
            hand.setAmount(hand.getAmount() - 1);
        p.setItemInHand(hand);
    }


}

