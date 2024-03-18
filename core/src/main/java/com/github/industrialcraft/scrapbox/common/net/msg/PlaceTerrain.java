package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.net.MessageC2S;

public class PlaceTerrain extends MessageC2S {
    public final Vector2 position;
    public final float radius;
    public PlaceTerrain(Vector2 position, float radius) {
        this.position = position;
        this.radius = radius;
    }
}
