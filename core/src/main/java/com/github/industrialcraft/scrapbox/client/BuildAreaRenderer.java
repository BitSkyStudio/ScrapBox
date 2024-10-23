package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;

public class BuildAreaRenderer {
    public FrameBuffer frameBuffer;
    public ArrayList<Rectangle> buildableAreas;
    private ShapeRenderer shapeRenderer;
    public BuildAreaRenderer() {
        this.frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        this.buildableAreas = new ArrayList<>();
        this.shapeRenderer = new ShapeRenderer();
        this.shapeRenderer.setAutoShapeType(true);
    }
    public void drawFrameBuffer(Camera camera){
        frameBuffer.bind();
        Gdx.gl.glClearColor(1, 0, 0, 0.4f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.gl.glDisable(GL20.GL_BLEND);

        Matrix4 uiMatrix = new Matrix4();
        uiMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.setProjectionMatrix(uiMatrix);

        shapeRenderer.setColor(1, 0, 0, 0.8f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        int lineWidth = 20;
        Vector3 projected = camera.project(new Vector3(camera.position.x/InGameScene.BOX_TO_PIXELS_RATIO, camera.position.y/InGameScene.BOX_TO_PIXELS_RATIO, 0));
        float shift = (projected.x-projected.y)%lineWidth;
        for(int i = 0;i < (Gdx.graphics.getWidth()+Gdx.graphics.getHeight())/lineWidth;i++){
            float x = i*lineWidth - Gdx.graphics.getHeight() + shift;
            shapeRenderer.line(x, Gdx.graphics.getHeight(), x+Gdx.graphics.getHeight(), 0);
        }
        shapeRenderer.end();

        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.setColor(0, 0, 0, 0);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for(Rectangle rectangle : buildableAreas){
            shapeRenderer.rect(rectangle.x*InGameScene.BOX_TO_PIXELS_RATIO, rectangle.y*InGameScene.BOX_TO_PIXELS_RATIO, rectangle.width*InGameScene.BOX_TO_PIXELS_RATIO, rectangle.height*InGameScene.BOX_TO_PIXELS_RATIO);
        }
        shapeRenderer.end();

        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for(Rectangle rectangle : buildableAreas){
            shapeRenderer.rect(rectangle.x*InGameScene.BOX_TO_PIXELS_RATIO, rectangle.y*InGameScene.BOX_TO_PIXELS_RATIO, rectangle.width*InGameScene.BOX_TO_PIXELS_RATIO, rectangle.height*InGameScene.BOX_TO_PIXELS_RATIO);
        }
        shapeRenderer.end();

        frameBuffer.end();
    }

    public void resize(int width, int height){
        frameBuffer.dispose();
        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
    }
    public void dispose(){
        frameBuffer.dispose();
        shapeRenderer.dispose();
    }
}
