package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;

public class CameraController {
    public final InGameScene scene;
    public final OrthographicCamera camera;
    public CameraController(InGameScene scene, OrthographicCamera camera) {
        this.scene = scene;
        this.camera = camera;
    }
    public void tick(){
        Vector2 move = new Vector2();
        if(Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)){
            move.y += 1;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)){
            move.y -= 1;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)){
            move.x -= 1;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
            move.x += 1;
        }
        move.scl(Gdx.graphics.getDeltaTime()*camera.zoom*500);
        scene.editors.forEach((integer, editor) -> {
            editor.window.moveBy(-move.x/camera.zoom, -move.y/camera.zoom);
        });
        camera.position.add(move.x, move.y, 0);
    }
    public void zoom(float value){
        camera.zoom = Math.min(Math.max(camera.zoom + value, 1), 10);
    }
}
