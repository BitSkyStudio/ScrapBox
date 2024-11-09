package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.util.HashMap;

public class FrameGameObject extends GameObject {
    public static final float INSIDE_SIZE = 1-0.09375f*2;

    public FrameGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
        super(position, rotation, server, config);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(1, 1);
        fixtureDef.shape = shape;
        fixtureDef.density = config.material.density;
        base.createFixture(fixtureDef);
        this.setBody("base", "frame", base);
    }

    @Override
    public float getMaxHealth() {
        return 100*config.material.baseHealthMultiplier;
    }

    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("up", new ConnectionEdge(new Vector2(0, 1), ConnectionEdgeType.Normal));
        edges.put("down", new ConnectionEdge(new Vector2(0, -1), ConnectionEdgeType.Normal));
        edges.put("left", new ConnectionEdge(new Vector2(-1, 0), ConnectionEdgeType.Normal));
        edges.put("right", new ConnectionEdge(new Vector2(1, 0), ConnectionEdgeType.Normal));
        edges.put("center", new ConnectionEdge(new Vector2(0, 0), ConnectionEdgeType.Internal));
        return edges;
    }

    @Override
    public String getType() {
        return "frame";
    }
}
