package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIElement;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILabel;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILink;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIRow;
import com.github.industrialcraft.scrapbox.server.ClientWorldManager;
import com.github.industrialcraft.scrapbox.server.EItemType;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.util.*;

public class GrabberGameObject extends GameObject {
    private ArrayList<Body> toConnect;
    private ArrayList<WeldJoint> connections;
    public GrabberGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
        super(position, rotation, server, config);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(1, 0.1875f);
        fixtureDef.shape = shape;
        fixtureDef.density = 1F;
        base.createFixture(fixtureDef);
        shape.setAsBox(0.9f, 0.0625f, new Vector2(0, 0.1875f), 0);
        fixtureDef.isSensor = true;
        fixtureDef.shape = shape;
        base.createFixture(fixtureDef);
        this.setBody("base", "grabber", base);

        this.toConnect = new ArrayList<>();
        this.connections = new ArrayList<>();
    }
    public static EnumMap<EItemType, Float> getItemCost(GameObjectConfig config){
        EnumMap<EItemType, Float> items = new EnumMap<>(EItemType.class);
        items.put(EItemType.Metal, 50f);
        items.put(EItemType.Circuit, 10f);
        items.put(EItemType.StickyResin, 30f);
        return items;
    }

    @Override
    public void getAnimationData(ClientWorldManager.AnimationData animationData) {
        animationData.addNumber("suction", this.getValueOnInput(0));
    }
    @Override
    public void tick() {
        super.tick();
        if(getValueOnInput(0) > 0){
            for(Body body : toConnect){
                WeldJointDef weld = new WeldJointDef();
                weld.initialize(getBaseBody(), body, getBaseBody().getPosition());
                connections.add((WeldJoint) server.physics.createJoint(weld));
            }
        } else {
            for(WeldJoint connection : connections){
                server.physics.destroyJoint(connection);
            }
            connections.clear();
        }
        toConnect.clear();
    }

    @Override
    public void onCollision(Fixture thisFixture, Fixture other) {
        if(thisFixture.isSensor()){
            if(other.getBody().getUserData() != null) {
                toConnect.add(other.getBody());
            }
        }
    }

    @Override
    public ArrayList<EditorUIRow> createEditorUI() {
        ArrayList<EditorUIRow> rows = new ArrayList<>();
        ArrayList<EditorUIElement> row = new ArrayList<>();
        row.add(new EditorUILabel("suction: "));
        row.add(new EditorUILink(0, true, defaultValues.getOrDefault(0, 0f), isInputFilled(0), false));
        rows.add(new EditorUIRow(row));
        return rows;
    }
    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("down", new ConnectionEdge(new Vector2(0, -0.1875f), ConnectionEdgeType.Normal));
        return edges;
    }
}
