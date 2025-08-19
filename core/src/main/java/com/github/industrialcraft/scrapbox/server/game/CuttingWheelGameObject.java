package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.EObjectInteractionMode;
import com.github.industrialcraft.scrapbox.common.Material;
import com.github.industrialcraft.scrapbox.server.EDamageType;
import com.github.industrialcraft.scrapbox.server.EItemType;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.util.EnumMap;

public class CuttingWheelGameObject extends BaseWheelGameObject{
    public CuttingWheelGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
        super(position, rotation, server, 0.01f, "wheel_join", "cutting_wheel", config);
    }
    public static EnumMap<EItemType, Float> getItemCost(GameObjectConfig config){
        EnumMap<EItemType, Float> items = new EnumMap<>(EItemType.class);
        items.put(config.<Material>getProperty(GameObjectConfig.Property.OMaterial).materialItem, 50f);
        return items;
    }
    @Override
    public void tick() {
        super.tick();
        if(motor.isMotorEnabled() && getLocalMode() != EObjectInteractionMode.Ghost)
            server.terrain.place("", getBody("wheel").getWorldCenter(), config.<Float>getProperty(GameObjectConfig.Property.Size) + .03f, false);
    }

    @Override
    public float getWheelFriction() {
        return 0;
    }

    @Override
    public void handleContact(GameObject gameObject) {
        if(motor.isMotorEnabled())
            gameObject.damage(3f/20f, EDamageType.Cutting);
    }
}
