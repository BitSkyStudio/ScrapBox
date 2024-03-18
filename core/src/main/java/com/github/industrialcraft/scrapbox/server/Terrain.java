package com.github.industrialcraft.scrapbox.server;

import clipper2.Clipper;
import clipper2.core.FillRule;
import clipper2.core.PathD;
import clipper2.core.PathsD;
import clipper2.core.PointD;
import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ShortArray;
import com.github.industrialcraft.scrapbox.common.net.msg.PlaceTerrain;

import java.util.ArrayList;
import java.util.Vector;

public class Terrain {
    public final Body body;
    private PathsD terrain;
    public Terrain(Server server) {
        this.body = server.physics.createBody(new BodyDef());

        /*FixtureDef fixtureDef = new FixtureDef();
        PolygonShape polygonShape = new PolygonShape();
        polygonShape.setAsBox(1,1);
        fixtureDef.shape = polygonShape;
        this.body.createFixture(fixtureDef);*/
        PathsD terrain = new PathsD();
        terrain.add(Clipper.Ellipse(new PointD(0, 0), 1, 1, 20));
        terrain.add(Clipper.Ellipse(new PointD(0.5, 0), 1, 1, 20));
        this.terrain = Clipper.Union(terrain, FillRule.Positive);
        this.rebuild();
    }
    public void place(PlaceTerrain placeTerrain){
        terrain.add(Clipper.Ellipse(new PointD(placeTerrain.position.x, placeTerrain.position.y), placeTerrain.radius, placeTerrain.radius, 20));
        this.terrain = Clipper.Union(terrain, FillRule.Positive);
        rebuild();
    }
    public void rebuild(){
        ArrayList<Fixture> fixtures = new ArrayList<>();
        for(Fixture fixture : this.body.getFixtureList()){
            fixtures.add(fixture);
        }
        for(Fixture fixture : fixtures){
            this.body.destroyFixture(fixture);
        }
        for(PathD path : this.terrain){
            ArrayList<Vector2> pathVec = new ArrayList<>();
            float[] points = new float[path.size() * 2];
            for(int i = 0;i < path.size();i++){
                pathVec.add(new Vector2((float) path.get(i).x, (float) path.get(i).y));
                points[i*2] = (float) path.get(i).x;
                points[(i*2)+1] = (float) path.get(i).y;
            }
            ShortArray array = new DelaunayTriangulator().computeTriangles(points, true);
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
            }

            //Box2DSeparator.separate(this.body, new FixtureDef(), points);
        }
    }
}
