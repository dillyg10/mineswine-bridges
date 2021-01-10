package com.mineswine.bridges.commands;

import com.mineswine.api.commands.temp.Command;
import com.mineswine.api.gameapi.GameAPI;
import com.mineswine.api.gameapi.language.Language;
import com.mineswine.api.gameapi.objects.GamePlayer;
import com.mineswine.api.gameapi.objects.Kit;
import com.mineswine.bridges.kits.BridgesKit;
import com.mineswine.bridges.kits.BridgesKitManager;
import com.mineswine.bridges.kits.BridgesKits;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by dylangellis on 2/10/15.
 */
public class CommandTest {
    BridgesKitManager kitManager;

    @Command(label = "testkit", help = {
            "§c/testkit kit <name> §f- Test a kit.",
            "§c/testkit ability <ability> §f- Just tests the ability of a kit."
    }, minargs = 2)
    public void testKit(CommandSender sender, String[] args) {
        if (kitManager == null) {
            kitManager = new BridgesKitManager();
            kitManager.register();
        }
        if (args[0].equals("kit")) {
            Kit kit = GameAPI.getGameStorage().getKits(Language.ENGLISH).get(args[1]);
            GamePlayer gamePlayer = GamePlayer.getPlayer((Player) sender);
            gamePlayer.setKit(kit);
            gamePlayer.equiptKit();
            kitManager.applyKitAbility(gamePlayer);
        } else {
            BridgesKit mcrlKit = new BridgesKit("dummy");
            mcrlKit.setAbility(Integer.parseInt(args[1]));
            GamePlayer gamePlayer = GamePlayer.getPlayer((Player) sender);
            gamePlayer.setKit(mcrlKit);
            kitManager.applyKitAbility(gamePlayer);
            sender.sendMessage("§cSet the ability of your kit too: " + BridgesKits.values()[Integer.parseInt(args[1])]);
        }
    }
}
