package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.server.Server;

public class SimpleWheelGameObject extends BaseWheelGameObject{
    public SimpleWheelGameObject(Vector2 position, float rotation, Server server) {
        super(position, rotation, server, 0.2f, "wheel_join", "wheel", 1);
    }

    @Override
    public String getType() {
        return "wheel";
    }
}
