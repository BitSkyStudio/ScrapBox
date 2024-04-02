package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIElement;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILabel;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILink;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIRow;
import com.github.industrialcraft.scrapbox.common.net.msg.OpenGameObjectEditUI;
import com.github.industrialcraft.scrapbox.common.net.msg.SetGameObjectEditUIData;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Player;
import com.github.industrialcraft.scrapbox.server.Server;

import java.util.ArrayList;
import java.util.HashMap;

public class ControllerGameObject extends GameObject {
    private final boolean[] inputs;
    public ControllerGameObject(Vector2 position, Server server) {
        super(position, server);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(FrameGameObject.INSIDE_SIZE, FrameGameObject.INSIDE_SIZE);
        fixtureDef.shape = shape;
        fixtureDef.density = 1F;
        base.createFixture(fixtureDef);
        this.setBody("base", "controller", base);

        this.inputs = new boolean[10];
    }
    @Override
    public void tick() {
        super.tick();
    }
    public void input(int key, boolean down){
        inputs[key] = down;
    }
    @Override
    public void requestEditorUI(Player player) {
        ArrayList<EditorUIRow> rows = new ArrayList<>();
        for(int i = 0;i < 10;i++){
            ArrayList<EditorUIElement> row = new ArrayList<>();
            row.add(new EditorUILabel((i+1)+":"));
            row.add(new EditorUILink(i, false));
            rows.add(new EditorUIRow(row));
        }
        player.send(new SetGameObjectEditUIData(this.getId(), rows));
    }

    @Override
    public float getValueOnOutput(int id) {
        return inputs[id]?1:0;
    }

    @Override
    public void createJoint(GameObjectConnectionEdge self, GameObjectConnectionEdge other) {
        float rotationOffset = (float) (Math.round((other.gameObject.getBaseBody().getAngle()-this.getBaseBody().getAngle())/HALF_PI)*HALF_PI);
        Transform transform = other.gameObject.getBaseBody().getTransform();
        this.getBaseBody().setTransform(transform.getPosition(), transform.getRotation()-rotationOffset);
        WeldJointDef joint = new WeldJointDef();
        joint.bodyA = this.getBaseBody();
        joint.bodyB = other.gameObject.getBaseBody();
        joint.localAnchorA.set(new Vector2(0, 0));
        joint.localAnchorB.set(new Vector2(0, 0));
        joint.referenceAngle = rotationOffset;
        this.server.physics.createJoint(joint);
    }

    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("center", new ConnectionEdge(new Vector2(0, 0), true));
        return edges;
    }
}
