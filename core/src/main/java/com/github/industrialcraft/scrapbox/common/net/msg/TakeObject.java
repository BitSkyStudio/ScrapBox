package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.net.MessageC2S;

public class TakeObject extends MessageC2S {
    public final String type;
    public final Vector2 position;
    public final Vector2 offset;
    public TakeObject(String type, Vector2 position, Vector2 offset) {
        this.type = type;
        this.position = position;
        this.offset = offset;
    }
}
