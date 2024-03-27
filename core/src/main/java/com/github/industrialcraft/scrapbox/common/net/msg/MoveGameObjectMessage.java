package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.net.EObjectInteractionMode;
import com.github.industrialcraft.scrapbox.common.net.MessageS2C;

public class MoveGameObjectMessage extends MessageS2C {
    public final int id;
    public final Vector2 position;
    public final float rotation;
    public final EObjectInteractionMode mode;
    public final boolean selected;
    public MoveGameObjectMessage(int id, Vector2 position, float rotation, EObjectInteractionMode mode, boolean selected) {
        this.id = id;
        this.position = position;
        this.rotation = rotation;
        this.mode = mode;
        this.selected = selected;
    }
}
