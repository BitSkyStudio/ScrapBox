package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.net.msg.PlaceTerrain;

public class CameraController {
    public final InGameScene scene;
    public final OrthographicCamera camera;
    public CameraController(InGameScene scene, OrthographicCamera camera) {
        this.scene = scene;
        this.camera = camera;
    }
    public void tick(){
        Vector2 move = new Vector2();
        if(ScrapBox.getSettings().UP.isDown()){
            move.y += 1;
        }
        if(ScrapBox.getSettings().DOWN.isDown()){
            move.y -= 1;
        }
        if(ScrapBox.getSettings().LEFT.isDown()){
            move.x -= 1;
        }
        if(ScrapBox.getSettings().RIGHT.isDown()){
            move.x += 1;
        }
        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT) && !move.isZero()){
            if (scene.toolBox.tool == ToolBox.Tool.TerrainModify && !scene.toolBox.isMouseInside()) {
                scene.connection.send(new PlaceTerrain(scene.toolBox.getSelectedTerrainType(), scene.mouseSelector.getWorldMousePosition(), 2*scene.toolBox.brushSize, scene.toolBox.brushRectangle));
            }
        }
        move.scl(Gdx.graphics.getDeltaTime()*camera.zoom*500);
        scene.editors.forEach((integer, editor) -> {
            editor.window.moveBy(-move.x/camera.zoom, -move.y/camera.zoom);
        });
        camera.position.add(move.x, move.y, 0);
    }
    public void zoom(float value){
        camera.zoom = (float) Math.min(Math.max(camera.zoom + value*Math.log(camera.zoom+1), 1), 100);
    }
}
