package com.github.industrialcraft.scrapbox.server;

import clipper2.Clipper;
import clipper2.core.FillRule;
import clipper2.core.PathD;
import clipper2.core.PathsD;
import clipper2.core.PointD;
import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ShortArray;
import com.github.industrialcraft.scrapbox.common.net.msg.PlaceTerrain;
import com.github.industrialcraft.scrapbox.common.net.msg.TerrainShapeMessage;

import java.util.*;

public class Terrain {
    public final Server server;
    public final Body body;
    public HashMap<String,PathsD> terrain;
    public Terrain(Server server) {
        this.server = server;
        this.body = server.physics.createBody(new BodyDef());

        /*FixtureDef fixtureDef = new FixtureDef();
        PolygonShape polygonShape = new PolygonShape();
        polygonShape.setAsBox(1,1);
        fixtureDef.shape = polygonShape;
        this.body.createFixture(fixtureDef);*/
        this.terrain = new HashMap<>();
        this.rebuild();
    }
    private PathsD getTerrainType(String terrainType){
        return this.terrain.getOrDefault(terrainType, new PathsD());
    }
    public void place(PlaceTerrain placeTerrain){
        //float resolution = (float) (2*placeTerrain.radius*Math.sin(Math.toRadians(22.5)));
        float resolution = 1f;
        PointD point = new PointD(Math.floor(placeTerrain.position.x/resolution)*resolution, Math.floor(placeTerrain.position.y/resolution)*resolution);
        PathD circle = createCircle(new Vector2((float) point.x, (float) point.y), placeTerrain.radius);
        if(placeTerrain.type.isEmpty()) {
            this.terrain.replaceAll((k, v) -> Clipper.Difference(this.terrain.get(k), new PathsD(Collections.singletonList(circle)), FillRule.Positive));
        } else {
            PathsD currentTerrain = getTerrainType(placeTerrain.type);
            currentTerrain.add(circle);
            currentTerrain = Clipper.Union(currentTerrain, FillRule.Positive);
            for(Map.Entry<String, PathsD> e : this.terrain.entrySet()){
                if(!e.getKey().equals(placeTerrain.type)){
                    currentTerrain = Clipper.Difference(currentTerrain, e.getValue(), FillRule.Positive);
                }
            }
            this.terrain.put(placeTerrain.type, currentTerrain);
        }
        rebuild();
    }
    private PathD createCircle(Vector2 position, float radius){
        PathD path = new PathD();
        for(int i = 0;i < 8;i++){
            float k = i + 0.5f;
            Vector2 offset = new Vector2((float) Math.cos(2*k*Math.PI/8), (float) Math.sin(2*k*Math.PI/8));
            path.add(new PointD(position.x + offset.x*radius, position.y + offset.y*radius));
        }
        return path;
    }
    public void rebuild(){
        ArrayList<Fixture> fixtures = new ArrayList<>();
        for(Fixture fixture : this.body.getFixtureList()){
            fixtures.add(fixture);
        }
        for(Fixture fixture : fixtures){
            this.body.destroyFixture(fixture);
        }
        //Clipper.SimplifyPaths(this.terrain, 0.5);
        //this.terrain = Clipper.Union(terrain, FillRule.Positive);

        TerrainShapeMessage terrainShapeMessage = this.createMessage();
        for(Player player : server.players){
            player.send(terrainShapeMessage);
        }

        for(PathsD type : this.terrain.values()){
            for(PathD path : type) {
                ChainShape shape = new ChainShape();
                ArrayList<Vector2> pathVec = new ArrayList<>();
                float[] points = new float[path.size() * 2];
                for (int i = 0; i < path.size(); i++) {
                    pathVec.add(new Vector2((float) path.get(i).x, (float) path.get(i).y));
                    points[i * 2] = (float) path.get(i).x;
                    points[(i * 2) + 1] = (float) path.get(i).y;
                }
                shape.createLoop(points);
                FixtureDef fixtureDef = new FixtureDef();
                fixtureDef.shape = shape;
                body.createFixture(fixtureDef);
            }
        }
    }
    public TerrainShapeMessage createMessage(){
        ArrayList<TerrainShapeMessage.TerrainData> terrain = new ArrayList<>();
        for(Map.Entry<String, PathsD> e : this.terrain.entrySet()){
            for(PathD path : e.getValue()) {
                ArrayList<Vector2> messagePath = new ArrayList<>();
                for (PointD point : path) {
                    messagePath.add(new Vector2((float) point.x, (float) point.y));
                }
                terrain.add(new TerrainShapeMessage.TerrainData(messagePath, e.getKey()));
            }
        }
        return new TerrainShapeMessage(terrain);
    }
}
