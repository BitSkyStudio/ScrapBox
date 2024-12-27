package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.github.industrialcraft.scrapbox.common.editui.*;
import com.github.industrialcraft.scrapbox.server.EItemType;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Player;
import com.github.industrialcraft.scrapbox.server.Server;
import fr.charleslabs.simplypid.SimplyPID;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

public class PIDControllerGameObject extends GameObject {
    private SimplyPID pid;
    private float lastOutput;
    public PIDControllerGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
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
        this.setBody("base", "pid_controller", base);

        this.pid = new SimplyPID(0, 0, 0, 0);
        this.lastOutput = 0;
    }
    public static EnumMap<EItemType, Float> getItemCost(GameObjectConfig config){
        EnumMap<EItemType, Float> items = new EnumMap<>(EItemType.class);
        items.put(EItemType.Metal, 30f);
        items.put(EItemType.Circuit, 50f);
        return items;
    }

    @Override
    public void load(DataInputStream stream) throws IOException {
        super.load(stream);
        this.pid = new SimplyPID(0, stream.readFloat(), stream.readFloat(), stream.readFloat());
    }

    @Override
    public void save(DataOutputStream stream) throws IOException {
        super.save(stream);
        stream.writeFloat((float) pid.getkP());
        stream.writeFloat((float) pid.getkI());
        stream.writeFloat((float) pid.getkD());
    }

    @Override
    public void tick() {
        super.tick();
        float error = getValueOnInput(0);
        this.lastOutput = (float) this.pid.getOutput(((double)server.getTicks())/Server.TPS, -error);
    }

    @Override
    public ArrayList<EditorUIRow> createEditorUI() {
        ArrayList<EditorUIRow> rows = new ArrayList<>();

        EditorUIRow p = new EditorUIRow(new ArrayList<>());
        p.elements.add(new EditorUILabel("P: "));
        p.elements.add(new EditorUIInputBox("p", ""+pid.getkP()));
        rows.add(p);

        EditorUIRow i = new EditorUIRow(new ArrayList<>());
        i.elements.add(new EditorUILabel("I: "));
        i.elements.add(new EditorUIInputBox("i", ""+pid.getkI()));
        rows.add(i);

        EditorUIRow d = new EditorUIRow(new ArrayList<>());
        d.elements.add(new EditorUILabel("D: "));
        d.elements.add(new EditorUIInputBox("d", ""+pid.getkD()));
        rows.add(d);

        EditorUIRow io = new EditorUIRow(new ArrayList<>());
        io.elements.add(new EditorUILink(0, true, defaultValues.getOrDefault(0, 0f), isInputFilled(0), false));
        io.elements.add(new EditorUILabel(" - "));
        io.elements.add(new EditorUILink(0, false, 0f, false, false));
        rows.add(io);

        return rows;
    }
    @Override
    public void handleEditorUIInput(String elementId, String value, Player player) {
        super.handleEditorUIInput(elementId, value, player);
        if(value.isEmpty())
            value = "0";
        if(elementId.equals("p")){
            float parsed = Float.parseFloat(value);
            pid.setkP(parsed);
        }
        if(elementId.equals("i")){
            float parsed = Float.parseFloat(value);
            pid.setkI(parsed);
        }
        if(elementId.equals("d")){
            float parsed = Float.parseFloat(value);
            pid.setkD(parsed);
        }
    }

    @Override
    public float getValueOnOutput(int id) {
        return lastOutput;
    }
    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("center", new ConnectionEdge(new Vector2(0, 0), ConnectionEdgeType.Internal));
        return edges;
    }
}
