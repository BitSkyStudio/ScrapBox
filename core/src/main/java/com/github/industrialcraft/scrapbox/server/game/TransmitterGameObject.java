package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.github.industrialcraft.scrapbox.common.editui.*;
import com.github.industrialcraft.scrapbox.server.EItemType;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Player;
import com.github.industrialcraft.scrapbox.server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

public class TransmitterGameObject extends GameObject {
    public int channel;
    public TransmitterGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
        super(position, rotation, server, config);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.6f, 0.45f);
        fixtureDef.shape = shape;
        fixtureDef.density = 1F;
        base.createFixture(fixtureDef);
        this.setBody("base", "transmitter", base);

        this.channel = 0;
    }

    @Override
    public void load(DataInputStream stream) throws IOException {
        super.load(stream);
        this.channel = stream.readInt();
    }
    @Override
    public void save(DataOutputStream stream) throws IOException {
        super.save(stream);
        stream.writeInt(channel);
    }

    public static EnumMap<EItemType, Float> getItemCost(GameObjectConfig config){
        EnumMap<EItemType, Float> items = new EnumMap<>(EItemType.class);
        items.put(EItemType.Metal, 30f);
        items.put(EItemType.Circuit, 10f);
        return items;
    }

    @Override
    public void tick() {
        super.tick();
        server.currentCommunications.put(channel, server.currentCommunications.getOrDefault(channel, 0f)+getValueOnInput(0));
    }

    @Override
    public ArrayList<EditorUIRow> createEditorUI() {
        ArrayList<EditorUIRow> rows = new ArrayList<>();
        ArrayList<EditorUIElement> row2 = new ArrayList<>();
        row2.add(new EditorUILabel("channel: "));
        row2.add(new EditorUIInputBox("channel", ""+channel));
        rows.add(new EditorUIRow(row2));
        ArrayList<EditorUIElement> row = new ArrayList<>();
        row.add(new EditorUILabel("transmit: "));
        row.add(new EditorUILink(0, true, defaultValues.getOrDefault(0, 0f), isInputFilled(0), false));
        rows.add(new EditorUIRow(row));
        return rows;
    }
    @Override
    public void handleEditorUIInput(String elementId, String value, Player player) {
        super.handleEditorUIInput(elementId, value, player);
        if(elementId.equals("channel")){
            try{
                this.channel = Integer.parseInt(value);
            } catch (NumberFormatException e){}
        }
    }

    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("down", new ConnectionEdge(new Vector2(0, -0.45f), ConnectionEdgeType.Normal));
        return edges;
    }
}
