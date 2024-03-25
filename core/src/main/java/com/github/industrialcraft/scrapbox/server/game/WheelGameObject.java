package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.util.HashMap;

public class WheelGameObject extends GameObject {
    public WheelGameObject(Vector2 position, Server server) {
        super(position, server);
    }
    @Override
    protected void add_fixtures() {
        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(1);
        fixtureDef.shape = shape;
        fixtureDef.density = 1F;
        this.body.createFixture(fixtureDef);
    }
    @Override
    public String get_type() {
        return "wheel";
    }
    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("up", new ConnectionEdge(new Vector2(0, 1), (float) (Math.PI/2*3)));
        return edges;
    }
}
