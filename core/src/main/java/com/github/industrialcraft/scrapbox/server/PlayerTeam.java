package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.math.Rectangle;
import com.github.industrialcraft.scrapbox.common.net.msg.PlayerTeamUpdate;
import com.github.industrialcraft.scrapbox.common.net.msg.UpdateBuildableAreas;
import com.github.industrialcraft.scrapbox.common.net.msg.UpdateInventory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;

public class PlayerTeam {
    public HashSet<Player> players;
    public ArrayList<Rectangle> buildableAreas;
    public EnumMap<EItemType, Float> inventory;
    public boolean infiniteItems;
    public PlayerTeam() {
        this.players = new HashSet<>();
        this.buildableAreas = new ArrayList<>();
        this.inventory = new EnumMap<>(EItemType.class);
        this.infiniteItems = true;
    }
    public void setInfiniteItems(boolean infiniteItems){
        this.infiniteItems = infiniteItems;
        syncInventory();
    }
    public float getItemCount(EItemType itemType){
        if(infiniteItems)
            return Float.POSITIVE_INFINITY;
        return this.inventory.getOrDefault(itemType, 0f);
    }
    public void removeItems(EItemType itemType, float count){
        if(infiniteItems)
            return;
        this.inventory.put(itemType, this.inventory.get(itemType)-count);
        syncInventory();
    }
    public void addItems(EItemType itemType, float count){
        if(infiniteItems)
            return;
        this.inventory.put(itemType, this.inventory.get(itemType)+count);
        syncInventory();
    }
    private void syncInventory(){
        players.forEach(player -> player.connection.send(new UpdateInventory(this.inventory.clone(), infiniteItems)));
    }
    public void setBuildableAreas(ArrayList<Rectangle> buildableAreas){
        this.buildableAreas = buildableAreas;
        players.forEach(player -> player.connection.send(new UpdateBuildableAreas(buildableAreas)));
    }
    public void syncPlayer(Player player, String teamName){
        player.connection.send(new UpdateInventory(this.inventory.clone(), infiniteItems));
        player.connection.send(new UpdateBuildableAreas(buildableAreas));
        player.connection.send(new PlayerTeamUpdate(teamName));
    }
    public boolean isInBuildableArea(float x, float y){
        if(buildableAreas.isEmpty())
            return true;
        for(Rectangle area : buildableAreas){
            if(area.contains(x, y))
                return true;
        }
        return false;
    }
}
