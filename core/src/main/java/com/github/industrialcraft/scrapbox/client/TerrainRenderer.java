package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.net.msg.TerrainShapeMessage;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TerrainRenderer {
    public static final float TERRAIN_TEXTURE_SIZE = 16;

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
        for(Map.Entry<String, TerrainShapeMessage.TerrainData> entry : message.terrain.entrySet()) {
            for (TerrainShapeMessage.TerrainPath path : entry.getValue().terrain) {
                org.poly2tri.geometry.polygon.Polygon polygon = new org.poly2tri.geometry.polygon.Polygon(path.points.stream().map(vector2 -> new PolygonPoint(vector2.x, vector2.y)).toArray(PolygonPoint[]::new));
                for(TerrainShapeMessage.TerrainPath hole : entry.getValue().holes){
                    polygon.addHole(new org.poly2tri.geometry.polygon.Polygon(hole.points.stream().map(vector2 -> new PolygonPoint(vector2.x, vector2.y)).toArray(PolygonPoint[]::new)));
                }
                Poly2Tri.triangulate(polygon);
                List<DelaunayTriangle> triangles = polygon.getTriangles();
                int pointIndex = 0;
                float[] points = new float[triangles.size()*3*2];
                for(DelaunayTriangle triangle : triangles){
                    for(TriangulationPoint point : triangle.points) {
                        points[pointIndex*2] = point.getXf() * TERRAIN_TEXTURE_SIZE;
                        points[pointIndex*2+1] = point.getYf() * TERRAIN_TEXTURE_SIZE;
                        pointIndex += 1;
                    }
                }
                short[] vertices = new short[points.length/2];
                for(int i = 0;i < vertices.length;i++){
                    vertices[i] = (short) i;
                }
                this.terrain.add(new PolygonRegion(this.textures.get(entry.getKey()), points, vertices));
            }
        }
    }
    public void draw(CameraController cameraController){
        this.polygonSpriteBatch.setProjectionMatrix(cameraController.camera.combined.cpy().scl(InGameScene.BOX_TO_PIXELS_RATIO / TERRAIN_TEXTURE_SIZE));
        this.polygonSpriteBatch.begin();
        for(PolygonRegion polygonRegion : this.terrain){
            this.polygonSpriteBatch.draw(polygonRegion, 0, 0);
        }
        this.polygonSpriteBatch.end();
    }
}
