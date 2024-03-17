package com.github.industrialcraft.scrapbox.server.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.server.net.MessageS2C;

public class MoveGameObjectMessage extends MessageS2C {
    public final int id;
    public final Vector2 position;
    public final float rotation;
    public MoveGameObjectMessage(int id, Vector2 position, float rotation) {
        this.id = id;
        this.position = position;
        this.rotation = rotation;
    }
}
