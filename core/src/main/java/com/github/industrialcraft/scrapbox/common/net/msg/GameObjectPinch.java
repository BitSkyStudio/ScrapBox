package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.net.MessageC2S;

public class GameObjectPinch extends MessageC2S {
    public final int id;
    public final Vector2 offset;
    public GameObjectPinch(int id, Vector2 offset) {
        this.id = id;
        this.offset = offset;
    }
}
