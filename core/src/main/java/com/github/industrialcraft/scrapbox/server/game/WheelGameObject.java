package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.WheelJointDef;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.util.HashMap;

public class WheelGameObject extends GameObject {
    public WheelGameObject(Vector2 position, Server server) {
        super(position, server);
    }

    @Override
    public void createJoint(GameObjectConnectionEdge self, GameObjectConnectionEdge other) {
        /*WheelJointDef joint = new WheelJointDef();
        joint.bodyA = this.body;
        joint.bodyB = other.gameObject.body;
        joint.localAnchorA.set(new Vector2(0, 0));
        joint.localAnchorB.set(new Vector2(0, -2));
        joint.localAxisA.set(new Vector2(0, -1));
        joint.dampingRatio = 1;
        joint.stiffness = 1000;*/
        RevoluteJointDef revoluteJoint = new RevoluteJointDef();
        revoluteJoint.bodyA = other.gameObject.body;
        revoluteJoint.bodyB = this.body;
        revoluteJoint.localAnchorA.set(new Vector2(0, -2));
        revoluteJoint.localAnchorA.set(new Vector2(0, -2));
        this.server.physics.createJoint(revoluteJoint);
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
