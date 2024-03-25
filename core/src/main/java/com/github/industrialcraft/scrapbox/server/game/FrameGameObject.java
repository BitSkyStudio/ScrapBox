package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.util.HashMap;

public class FrameGameObject extends GameObject {
    public FrameGameObject(Vector2 position, Server server) {
        super(position, server);
    }

    @Override
    public void createJoint(GameObjectConnectionEdge self, GameObjectConnectionEdge other) {
        RevoluteJointDef joint = new RevoluteJointDef();
        joint.bodyA = this.body;
        joint.bodyB = other.gameObject.body;
        joint.localAnchorA.set(self.connectionEdge.offset);
        joint.localAnchorB.set(other.connectionEdge.offset);
        joint.enableLimit = true;
        //System.out.println(weldCandidate.angle);
        //joint.referenceAngle = (float) -weldCandidate.angle;
        //joint.referenceAngle = (float) Math.PI;
        joint.referenceAngle = other.gameObject.body.getAngle() - this.body.getAngle();
        joint.lowerAngle = 0f;
        joint.upperAngle = 0f;
        this.server.physics.createJoint(joint);
    }

    @Override
    protected void add_fixtures() {
        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(1, 1);
        fixtureDef.shape = shape;
        fixtureDef.density = 1F;
        this.body.createFixture(fixtureDef);
    }
    @Override
    public String get_type() {
        return "frame";
    }
    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("up", new ConnectionEdge(new Vector2(0, 1), (float) (Math.PI/2*3)));
        edges.put("down", new ConnectionEdge(new Vector2(0, -1), (float) (Math.PI/2*1)));
        edges.put("left", new ConnectionEdge(new Vector2(-1, 0), (float) (Math.PI/2*2)));
        edges.put("right", new ConnectionEdge(new Vector2(1, 0), (float) (Math.PI/2*0)));
        return edges;
    }
}
