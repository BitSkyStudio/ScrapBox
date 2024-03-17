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
        Gdx.input.setInputProcessor(new InputProcessor() {
            @Override
            public boolean keyDown(int keycode) {
                return false;
            }
            @Override
            public boolean keyUp(int keycode) {
                return false;
            }
            @Override
            public boolean keyTyped(char character) {
                return false;
            }
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                return false;
            }
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                return false;
            }
            @Override
            public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
                return false;
            }
            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                return false;
            }
            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                return false;
            }
            @Override
            public boolean scrolled(float amountX, float amountY) {
                camera.zoom = Math.min(Math.max(camera.zoom + amountY, 1), 10);
                return false;
            }
        });
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
}
