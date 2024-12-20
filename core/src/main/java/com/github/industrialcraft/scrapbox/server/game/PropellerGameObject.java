package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIElement;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILabel;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILink;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIRow;
import com.github.industrialcraft.scrapbox.server.ClientWorldManager;
import com.github.industrialcraft.scrapbox.server.EItemType;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

public class PropellerGameObject extends GameObject {
    public float speed;
    public PropellerGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
        super(position, rotation, server, config);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(1, 0.25f);
        fixtureDef.shape = shape;
        fixtureDef.density = 1F;
        base.createFixture(fixtureDef);
        this.setBody("base", "propeller", base);

        this.speed = 0;
    }
    public static EnumMap<EItemType, Float> getItemCost(GameObjectConfig config){
        EnumMap<EItemType, Float> items = new EnumMap<>(EItemType.class);
        items.put(EItemType.Metal, 30f);
        items.put(EItemType.Transmission, 20f);
        items.put(EItemType.Circuit, 10f);
        return items;
    }

    @Override
    public void getAnimationData(ClientWorldManager.AnimationData animationData) {
        animationData.addNumber("speed", this.speed);
    }

    @Override
    public void tick() {
        super.tick();
        float targetSpeed = Math.max(Math.min(getValueOnInput(0),1),-1);
        float maxChangePerTick = 0.05f;
        float difference = Math.min(Math.max(this.speed - targetSpeed, -maxChangePerTick), maxChangePerTick);
        this.speed -= difference;
        float angle = getBaseBody().getAngle();
        getBaseBody().applyLinearImpulse(new Vector2((float) -Math.sin(angle), (float) Math.cos(angle)).scl(5*targetSpeed), getBaseBody().getPosition(), true);
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
        edges.put("down", new ConnectionEdge(new Vector2(0, -0.25f), ConnectionEdgeType.Normal));
        return edges;
    }
}
