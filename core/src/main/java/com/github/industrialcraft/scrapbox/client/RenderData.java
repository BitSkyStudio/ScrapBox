package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class RenderData {
    public final TextureRegion texture;
    public final float width;
    public final float height;
    public final CustomRenderFunction customRenderFunction;
    public RenderData(Texture texture, float width, float height) {
        this.texture = new TextureRegion(texture);
        this.width = width;
        this.height = height;
        this.customRenderFunction = null;
    }
    public RenderData(Texture texture, float width, float height, CustomRenderFunction customRenderFunction) {
        this.texture = new TextureRegion(texture);
        this.width = width;
        this.height = height;
        this.customRenderFunction = customRenderFunction;
    }
    public void draw(Batch batch, ClientGameObject gameObject){
        Vector2 lerpedPosition = gameObject.getRealPosition();
        batch.draw(this.texture, (lerpedPosition.x - this.width) * InGameScene.BOX_TO_PIXELS_RATIO, (lerpedPosition.y - this.height) * InGameScene.BOX_TO_PIXELS_RATIO, this.width * InGameScene.BOX_TO_PIXELS_RATIO, this.height * InGameScene.BOX_TO_PIXELS_RATIO, this.width * InGameScene.BOX_TO_PIXELS_RATIO * 2, this.height * InGameScene.BOX_TO_PIXELS_RATIO * 2, 1, 1, (float) Math.toDegrees(gameObject.getRealAngle()));
    }
    public void dispose(){
        this.texture.getTexture().dispose();
    }

    @FunctionalInterface
    public interface CustomRenderFunction{
        void render(RenderData renderData, ClientGameObject gameObject, Batch batch);
    }
}
