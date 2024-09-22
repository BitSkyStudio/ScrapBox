package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.*;
import com.github.industrialcraft.scrapbox.common.EObjectInteractionMode;
import com.github.industrialcraft.scrapbox.server.ClientWorldManager;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class RopeGameObject extends GameObject {
    public GameObject other;
    public RopeJoint joint;
    public RopeGameObject(Vector2 position, float rotation, Server server) {
        super(position, rotation, server);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(0.1f);
        fixtureDef.shape = shape;
        fixtureDef.density = 5F;
        base.createFixture(fixtureDef);
        this.setBody("base", "rope_connector", base);

        this.other = null;
        this.joint = null;
    }

    @Override
    public void getAnimationData(ClientWorldManager.AnimationData animationData) {
        if(joint != null)
            animationData.addNumber("length", joint.getMaxLength());
        if(other != null)
            animationData.addString("other", ""+other.getId());
    }

    @Override
    public void tick() {
        super.tick();
        if(other == null){
            RopeGameObject other = server.spawnGameObject(getBaseBody().getPosition(), getBaseBody().getAngle(), RopeGameObject::new, UUID.randomUUID());
            other.setMode(EObjectInteractionMode.Ghost);
            other.other = this;
            this.other = other;
            RopeJointDef jointDef = new RopeJointDef();
            jointDef.maxLength = 2f;
            jointDef.bodyA = this.getBaseBody();
            jointDef.bodyB = other.getBaseBody();
            jointDef.localAnchorA.setZero();
            jointDef.localAnchorB.setZero();
            RopeJoint joint = (RopeJoint) server.physics.createJoint(jointDef);
            this.joint = joint;
            other.joint = joint;
        } else if(other.isRemoved()){
            this.remove();
        }
    }

    @Override
    public void load(DataInputStream stream) throws IOException {
        super.load(stream);
        if(stream.readBoolean()) {
            this.other = server.getGameObjectByUUID(new UUID(stream.readLong(), stream.readLong()));
            if(other != null && this.getId() < other.getId()){
                RopeJointDef jointDef = new RopeJointDef();
                jointDef.maxLength = stream.readFloat();
                jointDef.bodyA = this.getBaseBody();
                jointDef.bodyB = other.getBaseBody();
                jointDef.localAnchorA.setZero();
                jointDef.localAnchorB.setZero();
                RopeJoint joint = (RopeJoint) server.physics.createJoint(jointDef);
                this.joint = joint;
                ((RopeGameObject)other).joint = joint;
            }
        }
    }

    @Override
    public void save(DataOutputStream stream) throws IOException {
        super.save(stream);
        stream.writeBoolean(other != null);
        if(other != null) {
            stream.writeLong(other.uuid.getMostSignificantBits());
            stream.writeLong(other.uuid.getLeastSignificantBits());
        }
        stream.writeFloat(joint==null?2:joint.getMaxLength());
    }

    @Override
    public Joint createJoint(String thisName, GameObject other, String otherName) {
        WeldJointDef joint = new WeldJointDef();
        joint.bodyA = this.getBaseBody();
        joint.bodyB = other.getBaseBody();
        joint.localAnchorA.set(this.getConnectionEdges().get(thisName).offset);
        joint.localAnchorB.set(other.getConnectionEdges().get(otherName).offset);
        return this.server.physics.createJoint(joint);
    }

    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("center", new ConnectionEdge(new Vector2(0, 0), false));
        return edges;
    }

    @Override
    public String getType() {
        return "rope";
    }
}
