package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIElement;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILabel;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILink;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIRow;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class BaseWheelGameObject extends GameObject {
    protected final RevoluteJoint motor;
    private final Body wheelBody;
    private final float adhesion;
    public BaseWheelGameObject(Vector2 position, float rotation, Server server, float adhesion, String joinType, String wheelType) {
        super(position, rotation, server);

        this.adhesion = adhesion;

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef baseFixtureDef = new FixtureDef();
        PolygonShape baseShape = new PolygonShape();
        baseShape.set(new Vector2[]{new Vector2(0, 0), new Vector2(1, 1), new Vector2(-1, 1)});
        baseFixtureDef.shape = baseShape;
        baseFixtureDef.density = 1F;
        base.createFixture(baseFixtureDef);
        this.setBody("base", joinType, base);


        bodyDef.bullet = true;
        this.wheelBody = server.physics.createBody(bodyDef);
        FixtureDef wheelFixtureDef = new FixtureDef();
        CircleShape wheelShape = new CircleShape();
        wheelShape.setRadius(0.95f);
        wheelFixtureDef.shape = wheelShape;
        wheelFixtureDef.density = 1F;
        wheelFixtureDef.friction = 500;
        wheelFixtureDef.restitution = 0;
        Fixture wheelFixture = wheelBody.createFixture(wheelFixtureDef);
        wheelFixture.setUserData("");
        RevoluteJointDef revoluteJoint = new RevoluteJointDef();
        revoluteJoint.bodyA = wheelBody;
        revoluteJoint.bodyB = base;
        revoluteJoint.localAnchorA.set(new Vector2(0, 0));
        revoluteJoint.localAnchorB.set(new Vector2(0, 0));
        revoluteJoint.maxMotorTorque = 1000;
        this.motor = (RevoluteJoint) this.server.physics.createJoint(revoluteJoint);
        this.setBody("wheel", wheelType, wheelBody);
    }

    @Override
    public void internalTick() {
        float value = Math.max(Math.min(getValueOnInput(0),1),-1);
        for(Contact contact : server.physics.getContactList()){
            if(contact.isTouching()){
                if((contact.getFixtureA().getBody().getUserData() == this && (contact.getFixtureA().getUserData() instanceof String)) || (contact.getFixtureB().getBody().getUserData() == this && (contact.getFixtureB().getUserData() instanceof String))){
                    if(contact.getFixtureA().getBody().getType() != BodyDef.BodyType.StaticBody && contact.getFixtureB().getBody().getType() != BodyDef.BodyType.StaticBody)
                        continue;
                    for(Vector2 point : contact.getWorldManifold().getPoints()){
                        if(point.isZero())
                            continue;
                        wheelBody.applyLinearImpulse(point.cpy().sub(wheelBody.getWorldCenter()).nor().scl(adhesion * this.vehicle.getMass()), wheelBody.getWorldCenter(), true);
                    }
                }
            }
        }
        if(value != 0){
            motor.enableMotor(true);
            motor.setMotorSpeed(value*12);
        } else {
            motor.enableMotor(false);
        }
    }

    @Override
    public ArrayList<EditorUIRow> createEditorUI() {
        ArrayList<EditorUIRow> rows = new ArrayList<>();
        ArrayList<EditorUIElement> row = new ArrayList<>();
        row.add(new EditorUILabel("speed: "));
        row.add(new EditorUILink(0, true, defaultValues.getOrDefault(0, 0f), isInputFilled(0), false));
        rows.add(new EditorUIRow(row));
        return rows;
    }

    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("up", new ConnectionEdge(new Vector2(0, 1), false));
        return edges;
    }
}
