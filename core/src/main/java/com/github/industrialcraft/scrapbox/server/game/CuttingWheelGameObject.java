package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.server.EDamageType;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

public class CuttingWheelGameObject extends BaseWheelGameObject{
    public CuttingWheelGameObject(Vector2 position, float rotation, Server server) {
        super(position, rotation, server, 0.01f, "wheel_join", "cutting_wheel");
    }
    @Override
    public void tick() {
        super.tick();
        if(motor.isMotorEnabled())
            server.terrain.place("", getBody("wheel").getWorldCenter(), 1.05f, false);
    }

    @Override
    public void handleContact(GameObject gameObject) {
        if(motor.isMotorEnabled())
            gameObject.damage(3f/20f, EDamageType.Cutting);
    }

    @Override
    public String getType() {
        return "cutting_wheel";
    }
}
