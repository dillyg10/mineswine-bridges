package com.mineswine.bridges.game;

import com.mineswine.api.gameapi.gamehandler.GameHandler;
import com.mineswine.api.gameapi.gamehandler.TeamHandler;
import com.mineswine.api.gameapi.gamemap.GameMap;
import com.mineswine.api.gameapi.objects.GamePlayer;
import com.mineswine.bridges.objects.BridgesPlayer;

/**
 * Created by dylangellis on 1/23/15.
 */
public class BridgesHandler extends GameHandler {


    @Override
    public void initted() {

    }

    @Override
    public void mapInitted(GameMap map) {
    }

    @Override
    public void lobbied() {
        TeamHandler.initTeams(4);
        for (GamePlayer player : getPlayers()) {
            ((BridgesPlayer) player).setDiedBefore(false);
        }
    }

    @Override
    public void started() {
        for (GamePlayer player : getPlayers()) {
            ((BridgesPlayer) player).setDiedBefore(false);
        }
    }

    @Override
    public void ended() {
        setRestarting(true);
    }

    @Override
    public void voteStarted() {

    }

    @Override
    public void voteEnded() {

    }

    @Override
    public void playerRemoved(GamePlayer player) {

    }

    @Override
    public void playerAdded(GamePlayer player) {

    }

    @Override
    public void lobbyTick() {

    }

    @Override
    public void gaveLobbyItems(GamePlayer player) {

    }

    @Override
    public boolean handleLoot() {
        return true;
    }

    @Override
    public boolean staticChest() {
        return true;
    }
}
