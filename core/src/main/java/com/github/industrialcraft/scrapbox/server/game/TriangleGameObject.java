package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.github.industrialcraft.scrapbox.server.EItemType;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.util.EnumMap;
import java.util.HashMap;

public class TriangleGameObject extends GameObject {
    public TriangleGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
        super(position, rotation, server, config);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        //shape.set(new Vector2[]{new Vector2(-1, -1), new Vector2(1, -1), getEndpoint(2)});
        shape.setAsBox(1, 0.1f);
        fixtureDef.shape = shape;
        fixtureDef.density = config.material.density;
        base.createFixture(fixtureDef);
        this.setBody("base", "triangle_arm", base);

        BodyDef endBodyDef = new BodyDef();
        endBodyDef.position.set(position.add(new Vector2(1*(float)Math.cos(getAngle())-1, 1*(float)Math.sin(getAngle())).rotateRad(rotation)));
        endBodyDef.angle = rotation + getAngle();
        endBodyDef.type = BodyDef.BodyType.DynamicBody;
        Body end = server.physics.createBody(endBodyDef);
        fixtureDef.shape = shape;
        fixtureDef.density = config.material.density;
        end.createFixture(fixtureDef);
        this.setBody("end", "triangle_arm", end, true);

        WeldJointDef joint = new WeldJointDef();
        joint.initialize(getBody("base"), getBody("end"), getBody("base").getPosition());
        server.physics.createJoint(joint);
    }
    private float getAngle(){
        return (float) Math.toRadians(Math.min(Math.max(Math.abs(config.size), 10), 90));
    }
    public static EnumMap<EItemType, Float> getItemCost(GameObjectConfig config){
        EnumMap<EItemType, Float> items = new EnumMap<>(EItemType.class);
        items.put(config.material.materialItem, 50f);
        return items;
    }
    @Override
    public float getMaxHealth() {
        return 100*config.material.baseHealthMultiplier;
    }

    @Override
    public boolean collidesWith(Fixture thisFixture, Fixture other) {
        for(ConnectionData connection : connections.values()){
            if(connection != null){
                if(connection.other == other.getBody().getUserData())
                    return false;
            }
        }
        return super.collidesWith(thisFixture, other);
    }

    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("down", new ConnectionEdge(new Vector2(0, -0.1f), ConnectionEdgeType.Normal));
        edges.put("up", new ConnectionEdge(new Vector2(0, 0.1f), ConnectionEdgeType.Normal, "end", 0));
        return edges;
    }
}
