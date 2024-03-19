package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.util.HashMap;

public class FrameGameObject extends GameObject {
    public FrameGameObject(Vector2 position, Server server) {
        super(position, server);
    }
    @Override
    protected void add_fixtures() {
        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(1, 1);
        fixtureDef.shape = shape;
        fixtureDef.density = 1;
        this.body.createFixture(fixtureDef);
    }
    @Override
    public String get_type() {
        return "frame";
    }
    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("up", new ConnectionEdge(new Vector2(1, 0), new Vector2(1, 0)));
        edges.put("down", new ConnectionEdge(new Vector2(-1, 0), new Vector2(-1, 0)));
        edges.put("left", new ConnectionEdge(new Vector2(0, -1), new Vector2(0, -1)));
        edges.put("right", new ConnectionEdge(new Vector2(0, 1), new Vector2(0, 1)));
        return edges;
    }
}
