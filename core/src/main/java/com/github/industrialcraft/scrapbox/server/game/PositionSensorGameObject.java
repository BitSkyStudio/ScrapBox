package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.github.industrialcraft.scrapbox.common.editui.*;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class PositionSensorGameObject extends GameObject {
    private PositionSensorCalibration calibration;

    public PositionSensorGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
        super(position, rotation, server, config);

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
        this.setBody("base", "position_sensor", base);

        this.calibration = null;
    }
    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void load(DataInputStream stream) throws IOException {
        super.load(stream);
        if(stream.readBoolean()){
            this.calibration = new PositionSensorCalibration(stream);
        } else {
            this.calibration = null;
        }
    }

    @Override
    public void save(DataOutputStream stream) throws IOException {
        super.save(stream);
        stream.writeBoolean(calibration != null);
        if(calibration != null){
            calibration.toStream(stream);
        }
    }
    @Override
    public ArrayList<EditorUIRow> createEditorUI() {
        ArrayList<EditorUIRow> rows = new ArrayList<>();

        ArrayList<String> calibrationSelection = new ArrayList<>();
        calibrationSelection.add("global");
        calibrationSelection.add("local");
        rows.add(new EditorUIRow(new ArrayList<>(Collections.singletonList(new EditorUIDropDown("calibration", calibrationSelection, calibration==null?0:1)))));

        String[] outputs = new String[]{"x", "y", "angle"};
        for(int i = 0;i < outputs.length;i++){
            ArrayList<EditorUIElement> row = new ArrayList<>();
            row.add(new EditorUILabel(outputs[i]+":"));
            row.add(new EditorUILink(i, false, 0f, isInputFilled(i), false));
            rows.add(new EditorUIRow(row));
        }
        return rows;
    }
    @Override
    public void handleEditorUIInput(String elementId, String value) {
        super.handleEditorUIInput(elementId, value);
        if(elementId.equals("calibration")){
            if(value.equals("0")){
                this.calibration = null;
            } else {
                this.calibration = new PositionSensorCalibration(getBaseBody().getPosition().cpy(), getBaseBody().getAngle());
            }
        }
    }

    @Override
    public float getValueOnOutput(int id) {
        Vector2 position = getBaseBody().getPosition().cpy();
        float rotation = getBaseBody().getAngle();
        if(calibration != null){
            position.sub(calibration.position);
            rotation -= calibration.rotation;
        }
        if(id == 0){
            return position.x;
        }
        if(id == 1){
            return position.y;
        }
        if(id == 2){
            float angle = (float) ((Math.toDegrees(-rotation)%360+360)%360);
            if(angle > 180)
                angle -= 360;
            return angle;
        }

        throw new RuntimeException("input id doesnt exist");
    }
    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("center", new ConnectionEdge(new Vector2(0, 0), ConnectionEdgeType.Internal));
        return edges;
    }

    @Override
    public String getType() {
        return "position_sensor";
    }

    public static class PositionSensorCalibration {
        public final Vector2 position;
        public final float rotation;
        public PositionSensorCalibration(Vector2 position, float rotation) {
            this.position = position;
            this.rotation = rotation;
        }
        public PositionSensorCalibration(DataInputStream stream) throws IOException {
            this.position = new Vector2(stream.readFloat(), stream.readFloat());
            this.rotation = stream.readFloat();
        }
        public void toStream(DataOutputStream stream) throws IOException {
            stream.writeFloat(position.x);
            stream.writeFloat(position.y);
            stream.writeFloat(rotation);
        }
    }
}
