package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;

public class CameraController {
    public final OrthographicCamera camera;
    public CameraController(OrthographicCamera camera) {
        this.camera = camera;
    }
    public void tick(){
        Vector2 move = new Vector2();
        if(Gdx.input.isKeyPressed(Input.Keys.W)){
            move.y += 1;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)){
            move.y -= 1;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A)){
            move.x -= 1;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D)){
            move.x += 1;
        }
        move.scl(Gdx.graphics.getDeltaTime()*camera.zoom*500);
        camera.position.add(move.x, move.y, 0);
    }
    public void zoom(float value){
        camera.zoom = Math.min(Math.max(camera.zoom + value, 1), 10);
    }
}
