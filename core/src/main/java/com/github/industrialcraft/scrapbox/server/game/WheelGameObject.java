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

public class WheelGameObject extends GameObject {
    private final RevoluteJoint motor;
    public WheelGameObject(Vector2 position, Server server) {
        super(position, server);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef baseFixtureDef = new FixtureDef();
        PolygonShape baseShape = new PolygonShape();
        baseShape.set(new Vector2[]{new Vector2(0, 0), new Vector2(1, 1), new Vector2(-1, 1)});
        baseFixtureDef.shape = baseShape;
        baseFixtureDef.density = 1F;
        base.createFixture(baseFixtureDef);
        this.setBody("base", "wheel_join", base);


        Body wheelBody = server.physics.createBody(bodyDef);
        FixtureDef wheelFixtureDef = new FixtureDef();
        CircleShape wheelShape = new CircleShape();
        wheelShape.setRadius(0.8f);
        wheelFixtureDef.shape = wheelShape;
        wheelFixtureDef.density = 1F;
        wheelBody.createFixture(wheelFixtureDef);
        RevoluteJointDef revoluteJoint = new RevoluteJointDef();
        revoluteJoint.bodyA = wheelBody;
        revoluteJoint.bodyB = base;
        revoluteJoint.localAnchorA.set(new Vector2(0, 0));
        revoluteJoint.localAnchorA.set(new Vector2(0, 0));
        revoluteJoint.maxMotorTorque = 100;
        this.motor = (RevoluteJoint) this.server.physics.createJoint(revoluteJoint);
        this.setBody("wheel", "wheel", wheelBody);
    }

    @Override
    public void tick() {
        super.tick();
        float value = Math.max(Math.min(getValueOnInput(0),1),-1);
        if(value != 0){
            motor.enableMotor(true);
            motor.setMotorSpeed(value*5);
        } else {
            motor.enableMotor(false);
        }
    }

    @Override
    public void requestEditorUI(Player player) {
        ArrayList<EditorUIRow> rows = new ArrayList<>();
        ArrayList<EditorUIElement> row = new ArrayList<>();
        row.add(new EditorUILabel("speed: "));
        row.add(new EditorUILink(0, true));
        rows.add(new EditorUIRow(row));
        player.send(new SetGameObjectEditUIData(this.getId(), rows));
    }

    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("up", new ConnectionEdge(new Vector2(0, 1), false));
        return edges;
    }
}
