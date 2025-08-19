package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.Material;
import com.github.industrialcraft.scrapbox.server.EItemType;
import com.github.industrialcraft.scrapbox.server.Server;

import java.util.EnumMap;

public class StickyWheelGameObject extends BaseWheelGameObject{
    public StickyWheelGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
        super(position, rotation, server, 2f, "wheel_join", "sticky_wheel", config);
    }
    public static EnumMap<EItemType, Float> getItemCost(GameObjectConfig config){
        EnumMap<EItemType, Float> items = new EnumMap<>(EItemType.class);
        items.put(config.<Material>getProperty(GameObjectConfig.Property.OMaterial).materialItem, 50f);
        items.put(EItemType.StickyResin, 10f);
        return items;
    }

    @Override
    public String getImpactSound() {
        return null;
    }
}
