package com.github.industrialcraft.scrapbox.server.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.server.net.MessageS2C;

public class AddGameObjectMessage extends MessageS2C {
    public final int id;
    public final String type;
    public final Vector2 position;
    public final float rotation;
    public AddGameObjectMessage(int id, String type, Vector2 position, float rotation) {
        this.id = id;
        this.type = type;
        this.position = position;
        this.rotation = rotation;
    }
}
