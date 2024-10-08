package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.server.Server;

public class StickyWheelGameObject extends BaseWheelGameObject{
    public StickyWheelGameObject(Vector2 position, float rotation, Server server) {
        super(position, rotation, server, 2f, "wheel_join", "sticky_wheel", 1);
    }

    @Override
    public String getType() {
        return "sticky_wheel";
    }
}
