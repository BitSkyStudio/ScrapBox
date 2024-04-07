package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.net.msg.TerrainShapeMessage;

import java.util.ArrayList;
import java.util.HashMap;

public class TerrainRenderer {
    public final HashMap<String, TextureRegion> textures;
    private final PolygonSpriteBatch polygonSpriteBatch;
    private final ArrayList<PolygonRegion> terrain;
    public TerrainRenderer() {
        this.textures = new HashMap<>();
        this.polygonSpriteBatch = new PolygonSpriteBatch();
        this.terrain = new ArrayList<>();
    }
    public void addTerrainType(String type, String texture){
        Texture texture1 = new Texture(texture);
        texture1.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        TextureRegion textureRegion = new TextureRegion(texture1);
        this.textures.put(type, textureRegion);
    }
    public void loadMessage(TerrainShapeMessage message){
        this.terrain.clear();
        for(TerrainShapeMessage.TerrainData path : message.terrain){
            float[] points = new float[path.points.size()*2];
            for(int i = 0;i < path.points.size();i++){
                points[i*2] = path.points.get(i).x * 16;
                points[(i*2)+1] = path.points.get(i).y * 16;
            }
            this.terrain.add(new PolygonRegion(this.textures.get(path.type), points, new EarClippingTriangulator().computeTriangles(points).toArray()));
        }
    }
    public void draw(CameraController cameraController){
        this.polygonSpriteBatch.setProjectionMatrix(cameraController.camera.combined.cpy().scl(InGameScene.BOX_TO_PIXELS_RATIO / 16));
        this.polygonSpriteBatch.begin();
        for(PolygonRegion polygonRegion : this.terrain){
            this.polygonSpriteBatch.draw(polygonRegion, 0, 0);
        }
        this.polygonSpriteBatch.end();
    }
}
