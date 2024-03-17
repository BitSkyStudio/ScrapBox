package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class Terrain {
    public final Body body;
    public Terrain(Server server) {
        this.body = server.physics.createBody(new BodyDef());

        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape polygonShape = new PolygonShape();
        polygonShape.setAsBox(1,1);
        fixtureDef.shape = polygonShape;
        this.body.createFixture(fixtureDef);
    }
}
