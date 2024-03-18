package com.github.industrialcraft.scrapbox.server;

import clipper2.Clipper;
import clipper2.core.FillRule;
import clipper2.core.PathD;
import clipper2.core.PathsD;
import clipper2.core.PointD;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

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
    public void rebuild(){
        ArrayList<Fixture> fixtures = new ArrayList<>();
        for(Fixture fixture : this.body.getFixtureList()){
            fixtures.add(fixture);
        }
        for(Fixture fixture : fixtures){
            this.body.destroyFixture(fixture);
        }
        for(PathD path : this.terrain){
            ArrayList<Vector2> points = new ArrayList<>();
            for(PointD pointD : path){
                points.add(new Vector2((float) pointD.x, (float) pointD.y));
            }
            Box2DSeparator.separate(this.body, new FixtureDef(), points);
        }
    }
}
