package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.github.industrialcraft.scrapbox.common.editui.*;
import com.github.industrialcraft.scrapbox.common.net.msg.SetGameObjectEditUIData;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Player;
import com.github.industrialcraft.scrapbox.server.Server;

import java.util.ArrayList;
import java.util.HashMap;

public class ControllerGameObject extends GameObject {
    public final boolean[] inputs;
    private final ControllerButtonData[] buttonData;
    public ControllerGameObject(Vector2 position, float rotation, Server server) {
        super(position, rotation, server);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
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
        this.buttonData = new ControllerButtonData[10];
        for(int i = 0;i < 10;i++){
            inputs[i] = false;
            buttonData[i] = new ControllerButtonData();
        }
    }
    @Override
    public void tick() {
        super.tick();
    }
    public void input(int key, boolean down){
        if(buttonData[key].keep){
            if(down){
                inputs[key] = !inputs[key];
            }
        } else {
            inputs[key] = down;
        }
    }
    @Override
    public void requestEditorUI(Player player) {
        ArrayList<EditorUIRow> rows = new ArrayList<>();

        ArrayList<String> holdSelection = new ArrayList<>();
        holdSelection.add("hold");
        holdSelection.add("keep");

        for(int i = 0;i < 10;i++){
            ArrayList<EditorUIElement> row = new ArrayList<>();
            int j = i+1;
            row.add(new EditorUILabel(j+":"));
            row.add(new EditorUIDropDown("hold"+j, holdSelection, buttonData[i].keep?1:0));
            row.add(new EditorUIInputBox("low"+j, buttonData[i].low+""));
            row.add(new EditorUIInputBox("high"+j, buttonData[i].high+""));
            row.add(new EditorUILink(i, false));
            rows.add(new EditorUIRow(row));
        }
        player.send(new SetGameObjectEditUIData(this.getId(), rows));
    }
    @Override
    public void handleEditorUIInput(String elementId, String value) {
        if(elementId.startsWith("hold")){
            int i = Integer.parseInt(elementId.replace("hold", ""))-1;
            buttonData[i].keep = !buttonData[i].keep;
        }
        try{
            if(elementId.startsWith("low")){
                int i = Integer.parseInt(elementId.replace("low", ""))-1;
                buttonData[i].low = Float.parseFloat(value);
            }
            if(elementId.startsWith("high")){
                int i = Integer.parseInt(elementId.replace("high", ""))-1;
                buttonData[i].high = Float.parseFloat(value);
            }
        } catch (Exception e){}
    }

    @Override
    public float getValueOnOutput(int id) {
        return inputs[id]?buttonData[id].high:buttonData[id].low;
    }

    @Override
    public Joint createJoint(GameObjectConnectionEdge self, GameObjectConnectionEdge other) {
        float rotationOffset = (float) (Math.round((other.gameObject.getBaseBody().getAngle()-this.getBaseBody().getAngle())/HALF_PI)*HALF_PI);
        Transform transform = other.gameObject.getBaseBody().getTransform();
        this.getBaseBody().setTransform(transform.getPosition(), transform.getRotation()-rotationOffset);
        WeldJointDef joint = new WeldJointDef();
        joint.bodyA = this.getBaseBody();
        joint.bodyB = other.gameObject.getBaseBody();
        joint.localAnchorA.set(new Vector2(0, 0));
        joint.localAnchorB.set(new Vector2(0, 0));
        joint.referenceAngle = rotationOffset;
        return this.server.physics.createJoint(joint);
    }

    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("center", new ConnectionEdge(new Vector2(0, 0), true));
        return edges;
    }

    @Override
    public String getType() {
        return "controller";
    }

    public static class ControllerButtonData{
        public boolean keep;
        private float low;
        private float high;
        public ControllerButtonData() {
            this.keep = false;
            this.low = 0;
            this.high = 1;
        }
    }
}
