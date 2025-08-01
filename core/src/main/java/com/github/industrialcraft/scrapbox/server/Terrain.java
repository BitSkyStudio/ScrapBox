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
    private final HashMap<String, TerrainType> terrainTypes;
    public boolean dirty;
    public Terrain(Server server) {
        this.server = server;
        this.body = server.physics.createBody(new BodyDef());
        this.terrain = new HashMap<>();
        this.terrainTypes = new HashMap<>();
        this.dirty = true;
    }
    private PathsD getTerrainType(String terrainType){
        return this.terrain.getOrDefault(terrainType, new PathsD());
    }

    public void placeFromMessage(PlaceTerrain placeTerrain){
        place(placeTerrain.type, placeTerrain.position, placeTerrain.radius, placeTerrain.rectangle);
    }
    public void place(String type, Vector2 point, float radius, boolean rectangle){
        PathD shape;
        if(rectangle){
            radius /= 2;
            shape = new PathD();
            shape.add(new PointD(point.x-radius, point.y-radius));
            shape.add(new PointD(point.x+radius, point.y-radius));
            shape.add(new PointD(point.x+radius, point.y+radius));
            shape.add(new PointD(point.x-radius, point.y+radius));
        } else {
            shape = Clipper.Ellipse(new PointD(point.x, point.y), radius, radius, 20);
        }
        float simplifyEpsilon = 0.05f;
        if(type.isEmpty()) {
            this.terrain.replaceAll((k, v) -> {
                PathsD terrain = Clipper.Difference(this.terrain.get(k), new PathsD(Collections.singletonList(shape)), FillRule.Positive);
                terrain = Clipper.SimplifyPaths(terrain, simplifyEpsilon);
                return terrain;
            });
        } else {
            PathsD currentTerrain = getTerrainType(type);
            currentTerrain.add(shape);
            currentTerrain = Clipper.Union(currentTerrain, FillRule.Positive);
            currentTerrain = Clipper.SimplifyPaths(currentTerrain, simplifyEpsilon);
            for(Map.Entry<String, PathsD> e : this.terrain.entrySet()){
                if(!e.getKey().equals(type)){
                    currentTerrain = Clipper.Difference(currentTerrain, e.getValue(), FillRule.Positive);
                }
            }
            this.terrain.put(type, currentTerrain);
        }
        dirty = true;
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

        TerrainShapeMessage terrainShapeMessage = this.createMessage();
        for(Player player : server.players){
            player.send(terrainShapeMessage);
        }
        for(Map.Entry<String, PathsD> type : this.terrain.entrySet()){
            for(PathD path : type.getValue()) {
                ChainShape shape = new ChainShape();
                ArrayList<Vector2> pathVec = new ArrayList<>();
                float[] points = new float[path.size() * 2];
                for (int i = 0; i < path.size(); i++) {
                    pathVec.add(new Vector2((float) path.get(i).x, (float) path.get(i).y));
                    points[i * 2] = (float) path.get(i).x;
                    points[(i * 2) + 1] = (float) path.get(i).y;
                }
                if(points.length <= 2*2){
                    shape.createChain(points);
                } else {
                    shape.createLoop(points);
                }
                FixtureDef fixtureDef = new FixtureDef();
                fixtureDef.shape = shape;
                TerrainType terrainType = this.terrainTypes.get(type.getKey());
                fixtureDef.friction = terrainType.friction;
                fixtureDef.restitution = terrainType.restitution;
                body.createFixture(fixtureDef).setUserData(this);
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
    public void registerTerrainType(String name, TerrainType type){
        this.terrainTypes.put(name, type);
    }
    public static class TerrainType {
        public final float friction;
        public final float restitution;
        public TerrainType(float friction, float restitution) {
            this.friction = friction;
            this.restitution = restitution;
        }
    }
}
