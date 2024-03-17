package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class RenderData {
    public final TextureRegion texture;
    public final float width;
    public final float height;
    public RenderData(Texture texture, float width, float height) {
        this.texture = new TextureRegion(texture);
        this.width = width;
        this.height = height;
    }
}
