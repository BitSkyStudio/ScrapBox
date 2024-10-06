package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.github.industrialcraft.scrapbox.common.editui.*;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;
import fr.charleslabs.simplypid.SimplyPID;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class TimerGameObject extends GameObject {
    private float[] values;
    private int head;
    public TimerGameObject(Vector2 position, float rotation, Server server) {
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
        this.setBody("base", "timer", base);
        resetValues(5);
    }
    @Override
    public void save(DataOutputStream stream) throws IOException {
        super.save(stream);
        stream.writeInt(head);
        stream.writeInt(values.length);
        for(float value : values){
            stream.writeFloat(value);
        }
    }
    @Override
    public void load(DataInputStream stream) throws IOException {
        super.load(stream);
        this.head = stream.readInt();
        this.values = new float[stream.readInt()];
        for(int i = 0;i < values.length;i++){
            this.values[i] = stream.readFloat();
        }
    }
    private void resetValues(float time){
        this.head = 0;
        this.values = new float[(int) (time*20)];
    }

    @Override
    public void tick() {
        super.tick();
        values[head] = getValueOnInput(0);
        head++;
        head %= values.length;
    }

    @Override
    public ArrayList<EditorUIRow> createEditorUI() {
        ArrayList<EditorUIRow> rows = new ArrayList<>();

        EditorUIRow p = new EditorUIRow(new ArrayList<>());
        p.elements.add(new EditorUILabel("time: "));
        p.elements.add(new EditorUIInputBox("time", ""+(((float)values.length)/20f)));
        rows.add(p);

        EditorUIRow io = new EditorUIRow(new ArrayList<>());
        io.elements.add(new EditorUILink(0, true, defaultValues.getOrDefault(0, 0f), isInputFilled(0), false));
        io.elements.add(new EditorUILabel(" - "));
        io.elements.add(new EditorUILink(0, false, 0f, false, false));
        rows.add(io);

        return rows;
    }
    @Override
    public void handleEditorUIInput(String elementId, String value) {
        super.handleEditorUIInput(elementId, value);
        if(elementId.equals("time")){
            float parsed = Float.parseFloat(value);
            resetValues(parsed);
        }
    }

    @Override
    public float getValueOnOutput(int id) {
        return values[head];
    }
    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("center", new ConnectionEdge(new Vector2(0, 0), ConnectionEdgeType.Internal));
        return edges;
    }

    @Override
    public String getType() {
        return "timer";
    }
}
