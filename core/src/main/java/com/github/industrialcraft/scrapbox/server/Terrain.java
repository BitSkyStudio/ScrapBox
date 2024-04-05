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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

public class Terrain {
    public final Server server;
    public final Body body;
    private PathsD terrain;
    public Terrain(Server server) {
        this.server = server;
        this.body = server.physics.createBody(new BodyDef());

        /*FixtureDef fixtureDef = new FixtureDef();
        PolygonShape polygonShape = new PolygonShape();
        polygonShape.setAsBox(1,1);
        fixtureDef.shape = polygonShape;
        this.body.createFixture(fixtureDef);*/
        PathsD terrain = new PathsD();
        terrain.add(Clipper.Ellipse(new PointD(0, 0), 2, 2, 8));
        terrain.add(Clipper.Ellipse(new PointD(1, 0), 2, 2, 8));
        this.terrain = Clipper.Union(terrain, FillRule.Positive);
        this.rebuild();
    }
    public void place(PlaceTerrain placeTerrain){
        //float resolution = (float) (2*placeTerrain.radius*Math.sin(Math.toRadians(22.5)));
        float resolution = 1f;
        PointD point = new PointD(Math.floor(placeTerrain.position.x/resolution)*resolution, Math.floor(placeTerrain.position.y/resolution)*resolution);
        PathD circle = createCircle(new Vector2((float) point.x, (float) point.y), placeTerrain.radius);
        if(placeTerrain.type.equals("dirt")) {
            terrain.add(circle);
            this.terrain = Clipper.Union(terrain, FillRule.Positive);
        } else {
            this.terrain = Clipper.Difference(terrain, new PathsD(Collections.singletonList(circle)), FillRule.Positive);
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

        for(PathD path : this.terrain){
            ChainShape shape = new ChainShape();
            ArrayList<Vector2> pathVec = new ArrayList<>();
            float[] points = new float[path.size() * 2];
            for(int i = 0;i < path.size();i++){
                pathVec.add(new Vector2((float) path.get(i).x, (float) path.get(i).y));
                points[i*2] = (float) path.get(i).x;
                points[(i*2)+1] = (float) path.get(i).y;
            }
            System.out.println(points.length);
            shape.createLoop(points);
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            body.createFixture(fixtureDef);
            /*ShortArray array = new EarClippingTriangulator().computeTriangles(points);
            for(int i = 0;i < (array.size/3);i++){
                ArrayList<Vector2> vertices = new ArrayList<>();
                vertices.add(pathVec.get(array.get((i*3))));
                vertices.add(pathVec.get(array.get((i*3)+1)));
                vertices.add(pathVec.get(array.get((i*3)+2)));
                FixtureDef fixtureDef = new FixtureDef();
                PolygonShape polyShape = new PolygonShape();
                polyShape.set(vertices.toArray(Vector2[]::new));
                fixtureDef.shape = polyShape;
                body.createFixture(fixtureDef);
            }*/
            /*ArrayList<Vector2> points = new ArrayList<>();
            for(int i = 0;i < path.size();i++) {
                points.add(new Vector2((float) path.get(i).x, (float) path.get(i).y));
            }
            try {
                Box2DSeparator.separate(this.body, new FixtureDef(), points);
            } catch (Throwable e){
                System.out.println(Box2DSeparator.validate(points));
            }*/
        }
    }
    public TerrainShapeMessage createMessage(){
        ArrayList<ArrayList<Vector2>> terrain = new ArrayList<>();
        for(PathD path : this.terrain){
            ArrayList<Vector2> messagePath = new ArrayList<>();
            for(PointD point : path){
                messagePath.add(new Vector2((float) point.x, (float) point.y));
            }
            terrain.add(messagePath);
        }
        return new TerrainShapeMessage(terrain);
    }
}
