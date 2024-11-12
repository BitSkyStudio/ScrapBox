package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.function.Predicate;

public class MouseSelector {
    public final InGameScene game;
    public MouseSelector(InGameScene game) {
        this.game = game;
    }
    public Selection getSelected(Predicate<ClientGameObject> predicate){
        Vector2 mouse = getWorldMousePosition();
        Selection selection = null;
        for(int i : game.gameObjects.keySet()){
            ClientGameObject gameObject = game.gameObjects.get(i);
            if(!gameObject.selectable || !predicate.test(gameObject)){
                continue;
            }
            RenderData renderData = game.renderDataRegistry.get(gameObject.type);
            float xDiff = mouse.x - gameObject.position.x;
            float yDiff = mouse.y - gameObject.position.y;
            float angle = (float) Math.atan2(xDiff, yDiff) + gameObject.rotation;
            float distance = (float) Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
            float widthScale = renderData.configScalingW?gameObject.config.size:1;
            float heightScale = renderData.configScalingH?gameObject.config.size:1;
            if((Math.abs(Math.sin(angle)) * distance < renderData.width * widthScale) && (Math.abs(Math.cos(angle)) * distance < renderData.height * heightScale)){
                Selection newSelection = new Selection(i, xDiff, yDiff, renderData.width * renderData.height);
                if(selection == null || (newSelection.size < selection.size)){
                    selection = newSelection;
                }
            }
        }
        return selection;
    }
    public Selection getSelected(){
        return getSelected(clientGameObject -> true);
    }
    public Vector2 getWorldMousePosition(){
        Vector3 screen = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        Vector3 world = game.cameraController.camera.unproject(screen);
        return new Vector2(world.x / InGameScene.BOX_TO_PIXELS_RATIO, world.y / InGameScene.BOX_TO_PIXELS_RATIO);
    }
    public static class Selection{
        public final int id;
        public final float offsetX;
        public final float offsetY;
        private final float size;
        public Selection(int id, float offsetX, float offsetY, float size) {
            this.id = id;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.size = size;
        }
    }
}
