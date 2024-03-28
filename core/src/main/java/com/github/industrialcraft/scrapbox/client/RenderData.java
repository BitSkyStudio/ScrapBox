package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
    public void draw(Batch batch, ClientGameObject gameObject){
        batch.draw(this.texture, (gameObject.position.x - this.width) * InGameScene.BOX_TO_PIXELS_RATIO, (gameObject.position.y - this.height) * InGameScene.BOX_TO_PIXELS_RATIO, this.width * InGameScene.BOX_TO_PIXELS_RATIO, this.height * InGameScene.BOX_TO_PIXELS_RATIO, this.width * InGameScene.BOX_TO_PIXELS_RATIO * 2, this.height * InGameScene.BOX_TO_PIXELS_RATIO * 2, 1, 1, (float) Math.toDegrees(gameObject.rotation));
    }
    public void dispose(){
        this.texture.getTexture().dispose();
    }
}
