package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.util.HashMap;

public class BalloonGameObject extends GameObject {
    public BalloonGameObject(Vector2 position, Server server) {
        super(position, server);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef baseFixtureDef = new FixtureDef();
        CircleShape baseShape = new CircleShape();
        baseShape.setRadius(0.8f);
        baseFixtureDef.shape = baseShape;
        baseFixtureDef.density = 1F;
        base.createFixture(baseFixtureDef);
        this.setBody("base", "balloon", base);
    }

    @Override
    public void tick() {
        getBaseBody().applyForceToCenter(new Vector2(0, 500), true);
    }

    @Override
    public void createJoint(GameObjectConnectionEdge self, GameObjectConnectionEdge other) {
        DistanceJointDef joint = new DistanceJointDef();
        joint.bodyA = this.getBaseBody();
        joint.bodyB = other.gameObject.getBaseBody();
        joint.localAnchorA.set(self.connectionEdge.offset);
        joint.localAnchorB.set(other.connectionEdge.offset);
        joint.length = 0.5f;
        this.server.physics.createJoint(joint);
    }

    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("down", new ConnectionEdge(new Vector2(0, -1), false));
        return edges;
    }
}
