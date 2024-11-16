package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.server.EItemType;
import com.github.industrialcraft.scrapbox.server.Server;

import java.util.EnumMap;

public class SimpleWheelGameObject extends BaseWheelGameObject{
    public SimpleWheelGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
        super(position, rotation, server, 0.2f, "wheel_join", "wheel", config);
    }
    public static EnumMap<EItemType, Float> getItemCost(GameObjectConfig config){
        EnumMap<EItemType, Float> items = new EnumMap<>(EItemType.class);
        items.put(config.material.materialItem, 50f);
        return items;
    }
}
