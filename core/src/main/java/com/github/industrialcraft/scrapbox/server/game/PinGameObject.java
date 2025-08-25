package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.github.industrialcraft.scrapbox.common.EObjectInteractionMode;
import com.github.industrialcraft.scrapbox.server.EDamageType;
import com.github.industrialcraft.scrapbox.server.EItemType;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.util.EnumMap;
import java.util.HashMap;

public class PinGameObject extends GameObject {
    public PinGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
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
        fixtureDef.density = 1f;
        base.createFixture(fixtureDef);
        this.setBody("base", "pin", base);
    }
    public static EnumMap<EItemType, Float> getItemCost(GameObjectConfig config){
        EnumMap<EItemType, Float> items = new EnumMap<>(EItemType.class);

        return items;
    }

    @Override
    public void setMode(EObjectInteractionMode mode) {
        super.setMode(mode==EObjectInteractionMode.Ghost?mode:EObjectInteractionMode.Static);
        this.localMode = mode;
    }

    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("up", new ConnectionEdge(new Vector2(0, 1), ConnectionEdgeType.Normal));
        edges.put("down", new ConnectionEdge(new Vector2(0, -1), ConnectionEdgeType.Normal));
        edges.put("left", new ConnectionEdge(new Vector2(-1, 0), ConnectionEdgeType.Normal));
        edges.put("right", new ConnectionEdge(new Vector2(1, 0), ConnectionEdgeType.Normal));
        return edges;
    }
    @Override
    public void damage(float amount, EDamageType damageType) {

    }
}
