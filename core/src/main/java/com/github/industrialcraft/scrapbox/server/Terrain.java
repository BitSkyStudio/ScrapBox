package com.github.industrialcraft.scrapbox.server;

import clipper2.Clipper;
import clipper2.core.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.github.industrialcraft.scrapbox.common.net.msg.PlaceTerrain;
import com.github.industrialcraft.scrapbox.common.net.msg.TerrainShapeMessage;

import java.util.*;
import java.util.stream.Collectors;

public class Terrain {
    public final Server server;
    public final Body body;
    public HashMap<String,PathsD> terrain;
    private boolean dirty;
    public Terrain(Server server) {
        this.server = server;
        this.body = server.physics.createBody(new BodyDef());
        this.terrain = new HashMap<>();
        this.dirty = true;
    }
    private PathsD getTerrainType(String terrainType){
        return this.terrain.getOrDefault(terrainType, new PathsD());
    }

    public void placeFromMessage(PlaceTerrain placeTerrain){
        place(placeTerrain.type, placeTerrain.position, placeTerrain.radius);
    }
    public void place(String type, Vector2 position, float radius){
        float resolution = radius/2;
        Vector2 point = new Vector2((float) (Math.floor(position.x/resolution)*resolution), (float) (Math.floor(position.y/resolution)*resolution));

        PathD circle = createCircle(point, radius);
        if(type.isEmpty()) {
            this.terrain.replaceAll((k, v) -> Clipper.Difference(this.terrain.get(k), new PathsD(Collections.singletonList(circle)), FillRule.Positive));
        } else {
            PathsD currentTerrain = getTerrainType(type);
            currentTerrain.add(circle);
            currentTerrain = Clipper.Union(currentTerrain, FillRule.Positive);
            for(Map.Entry<String, PathsD> e : this.terrain.entrySet()){
                if(!e.getKey().equals(type)){
                    currentTerrain = Clipper.Difference(currentTerrain, e.getValue(), FillRule.Positive);
                }
            }
            this.terrain.put(type, currentTerrain);
        }
        dirty = true;
    }
    private PathD createCircle(Vector2 position, float radius){
        final Vector2[] positions = new Vector2[]{new Vector2(-1, 2), new Vector2(1, 2), new Vector2(2, 1), new Vector2(2, -1), new Vector2(1, -2), new Vector2(-1, -2), new Vector2(-2, -1), new Vector2(-2, 1)};
        PathD path = new PathD();
        for(int i = 0;i < 8;i++){
            Vector2 offset = positions[7-i];
            path.add(new PointD(position.x + offset.x*radius/2, position.y + offset.y*radius/2));
        }
        return path;
    }
    public void rebuildIfNeeded(){
        if(!dirty){
            return;
        }
        dirty = false;
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
        HashMap<String,ArrayList<TerrainShapeMessage.TerrainData>> terrain = new HashMap<>();
        for(Map.Entry<String, PathsD> e : this.terrain.entrySet()){
            ArrayList<TerrainShapeMessage.TerrainData> terrainDatas = new ArrayList<>();
            HashMap<RectD,PathD> holes = new HashMap<>();
            for(PathD path : e.getValue()) {
                if(!Clipper.IsPositive(path)){
                    holes.put(GetBounds(path), Clipper.ReversePath(path));
                }
            }
            for(PathD path : e.getValue()) {
                if(Clipper.IsPositive(path)){
                    RectD bounds = GetBounds(path);
                    PathsD realHoles = new PathsD();
                    holes.forEach((rectD, pointDS) -> {
                        if(bounds.Contains(rectD)){
                            realHoles.add(pointDS);
                        }
                    });
                    Clipper.Union(realHoles, FillRule.Positive);
                    terrainDatas.add(new TerrainShapeMessage.TerrainData(createTerrainPath(path),new ArrayList<>(realHoles.stream().map(this::createTerrainPath).collect(Collectors.toList()))));
                }
            }
            terrain.put(e.getKey(), terrainDatas);
        }
        return new TerrainShapeMessage(terrain);
    }
    private RectD GetBounds(PathD path) {
        RectD result = new RectD(false);
        for (PointD pt : path) {
            if (pt.x < result.left) {
                result.left = pt.x;
            }
            if (pt.x > result.right) {
                result.right = pt.x;
            }
            if (pt.y < result.top) {
                result.top = pt.y;
            }
            if (pt.y > result.bottom) {
                result.bottom = pt.y;
            }
        }
        return result.left == Double.MAX_VALUE ? new RectD() : result;
    }
    private TerrainShapeMessage.TerrainPath createTerrainPath(PathD path){
        ArrayList<Vector2> messagePath = new ArrayList<>();
        for (PointD point : path) {
            messagePath.add(new Vector2((float) point.x, (float) point.y));
        }
        return new TerrainShapeMessage.TerrainPath(messagePath);
    }
}
