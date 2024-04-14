package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIElement;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILabel;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILink;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIRow;
import com.github.industrialcraft.scrapbox.common.net.msg.SetGameObjectEditUIData;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Player;
import com.github.industrialcraft.scrapbox.server.Server;

import java.util.ArrayList;
import java.util.HashMap;

public class RotatorGameObject extends GameObject {
    private final RevoluteJoint motor;
    public RotatorGameObject(Vector2 position, float rotation, Server server) {
        super(position, rotation, server);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef baseFixtureDef = new FixtureDef();
        PolygonShape baseShape = new PolygonShape();
        baseShape.set(new Vector2[]{new Vector2(0, 0), new Vector2(1, -1), new Vector2(-1, -1)});
        baseFixtureDef.shape = baseShape;
        baseFixtureDef.density = 1F;
        base.createFixture(baseFixtureDef);
        this.setBody("base", "rotator_join", base);


        Body wheelBody = server.physics.createBody(bodyDef);
        FixtureDef wheelFixtureDef = new FixtureDef();
        PolygonShape wheelShape = new PolygonShape();
        wheelShape.set(new Vector2[]{new Vector2(0, -1), new Vector2(1, 0), new Vector2(-1, 0)});
        wheelFixtureDef.shape = wheelShape;
        wheelFixtureDef.density = 1F;
        wheelBody.createFixture(wheelFixtureDef);
        RevoluteJointDef revoluteJoint = new RevoluteJointDef();
        revoluteJoint.bodyA = wheelBody;
        revoluteJoint.bodyB = base;
        revoluteJoint.localAnchorA.set(new Vector2(0, -1));
        revoluteJoint.localAnchorB.set(new Vector2(0, 0));
        revoluteJoint.maxMotorTorque = 10000;
        revoluteJoint.lowerAngle = (float) (-Math.PI/2);
        revoluteJoint.upperAngle = (float) (Math.PI/2);
        revoluteJoint.enableLimit = true;
        revoluteJoint.enableMotor = true;
        this.motor = (RevoluteJoint) this.server.physics.createJoint(revoluteJoint);
        this.setBody("end", "rotator_end", wheelBody);
    }

    @Override
    public void tick() {
        super.tick();
        float value = Math.max(Math.min(getValueOnInput(0),90),-90);
        motor.setMotorSpeed((float) -(Math.toDegrees(motor.getJointAngle())-value)/10);
    }

    @Override
    public Joint createJoint(String thisName, GameObject other, String otherName) {
        if(thisName.equals("rotator")) {
            RevoluteJointDef joint = new RevoluteJointDef();
            joint.bodyA = this.getBody("end");
            joint.bodyB = other.getBaseBody();
            joint.localAnchorA.set(new Vector2(0, 0));
            joint.localAnchorB.set(other.getConnectionEdges().get(otherName).offset);
            joint.enableLimit = true;
            //System.out.println(weldCandidate.angle);
            //joint.referenceAngle = (float) -weldCandidate.angle;
            //joint.referenceAngle = (float) Math.PI;
            joint.referenceAngle = (float) (Math.round((other.getBaseBody().getAngle() - this.getBody("end").getAngle())/HALF_PI)*HALF_PI);
            joint.lowerAngle = 0f;
            joint.upperAngle = 0f;
            return this.server.physics.createJoint(joint);
        } else{
            return super.createJoint(thisName, other, otherName);
        }
    }

    @Override
    public void requestEditorUI(Player player) {
        ArrayList<EditorUIRow> rows = new ArrayList<>();
        ArrayList<EditorUIElement> row = new ArrayList<>();
        row.add(new EditorUILabel("angle: "));
        row.add(new EditorUILink(0, true, defaultValues.getOrDefault(0, 0f)));
        rows.add(new EditorUIRow(row));
        player.send(new SetGameObjectEditUIData(this.getId(), rows));
    }

    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("down", new ConnectionEdge(new Vector2(0, -1), false));
        Body rotatorBody = getBody("end");
        edges.put("rotator", new ConnectionEdge(getBaseBody().getLocalPoint(rotatorBody.getWorldPoint(new Vector2(0, 0))), false));
        return edges;
    }

    @Override
    public String getType() {
        return "rotator";
    }
}
