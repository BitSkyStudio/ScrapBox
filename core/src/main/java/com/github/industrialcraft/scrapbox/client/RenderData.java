package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.Material;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.tommyettinger.colorful.rgb.ColorfulBatch;

public class RenderData {
    public final TextureRegion texture;
    public TextureRegion materialTexture;
    public final float width;
    public final float height;
    public final CustomRenderFunction customRenderFunction;
    public boolean configScalingW;
    public boolean configScalingH;
    public RenderData(Texture texture, float width, float height) {
        this.texture = texture==null?null:new TextureRegion(texture);
        this.width = width;
        this.height = height;
        this.customRenderFunction = null;
        this.configScalingW = false;
        this.configScalingH = false;
    }
    public RenderData(Texture texture, float width, float height, CustomRenderFunction customRenderFunction) {
        this.texture = texture==null?null:new TextureRegion(texture);
        this.width = width;
        this.height = height;
        this.customRenderFunction = customRenderFunction;
        this.configScalingW = false;
        this.configScalingH = false;
    }
    public RenderData setConfigScalingEnabled(boolean width, boolean height){
        this.configScalingW = width;
        this.configScalingH = height;
        return this;
    }
    public RenderData addMaterialTexture(Texture materialTexture){
        this.materialTexture = new TextureRegion(materialTexture);
        return this;
    }
    public void draw(Batch batch, ClientGameObject gameObject){
        Vector2 lerpedPosition = gameObject.getRealPosition();
        float size = gameObject.config.getProperty(GameObject.GameObjectConfig.Property.Size);
        float widthScale = configScalingW?size:1;
        float heightScale = configScalingH?size:1;
        Material material = gameObject.config.getProperty(GameObject.GameObjectConfig.Property.OMaterial);
        if(this.materialTexture != null){
            Color color = material.color;
            ((ColorfulBatch)batch).setTweak(color.r, color.g, color.b, 0.5f);
            batch.draw(this.materialTexture, (lerpedPosition.x - this.width * widthScale) * InGameScene.BOX_TO_PIXELS_RATIO, (lerpedPosition.y - this.height * heightScale) * InGameScene.BOX_TO_PIXELS_RATIO, this.width * widthScale * InGameScene.BOX_TO_PIXELS_RATIO, this.height * heightScale * InGameScene.BOX_TO_PIXELS_RATIO, this.width * widthScale * InGameScene.BOX_TO_PIXELS_RATIO * 2, this.height * heightScale * InGameScene.BOX_TO_PIXELS_RATIO * 2, 1, 1, (float) Math.toDegrees(gameObject.getRealAngle()));
            ((ColorfulBatch)batch).setTweak(ColorfulBatch.TWEAK_RESET);
        }
        if(this.texture != null)
            batch.draw(this.texture, (lerpedPosition.x - this.width * widthScale) * InGameScene.BOX_TO_PIXELS_RATIO, (lerpedPosition.y - this.height * heightScale) * InGameScene.BOX_TO_PIXELS_RATIO, this.width * widthScale * InGameScene.BOX_TO_PIXELS_RATIO, this.height * heightScale * InGameScene.BOX_TO_PIXELS_RATIO, this.width * widthScale * InGameScene.BOX_TO_PIXELS_RATIO * 2, this.height * heightScale * InGameScene.BOX_TO_PIXELS_RATIO * 2, 1, 1, (float) Math.toDegrees(gameObject.getRealAngle()));
    }
    public void dispose(){
        if(this.texture != null)
            this.texture.getTexture().dispose();
        if(this.materialTexture != null)
            this.materialTexture.getTexture().dispose();
    }

    @FunctionalInterface
    public interface CustomRenderFunction{
        void render(RenderData renderData, ClientGameObject gameObject, Batch batch);
    }
}
