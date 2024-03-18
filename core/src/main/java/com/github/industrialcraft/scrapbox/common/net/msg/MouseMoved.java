package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.net.MessageC2S;

public class MouseMoved extends MessageC2S {
    public final Vector2 position;
    public MouseMoved(Vector2 position) {
        this.position = position;
    }
}
