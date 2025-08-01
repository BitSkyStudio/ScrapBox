package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.github.industrialcraft.scrapbox.common.EObjectInteractionMode;
import com.github.industrialcraft.scrapbox.common.editui.*;
import com.github.industrialcraft.scrapbox.server.ClientWorldManager;
import com.github.industrialcraft.scrapbox.server.EItemType;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class DistanceSensorGameObject extends GameObject {
    private float max;
    public DistanceSensorGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
        super(position, rotation, server, config);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.45f, 0.15f);
        fixtureDef.shape = shape;
        fixtureDef.density = 1F;
        base.createFixture(fixtureDef);
        this.max = 100;
        this.setBody("base", "distance_sensor", base);
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
    }

    @Override
    public ArrayList<EditorUIRow> createEditorUI() {
        ArrayList<EditorUIRow> rows = new ArrayList<>();
        ArrayList<EditorUIElement> row = new ArrayList<>();
        row.add(new EditorUILabel("distance: "));
        row.add(new EditorUILink(0, false, 0f, false, false));
        rows.add(new EditorUIRow(row));
        return rows;
    }

    @Override
    public float getValueOnOutput(int id) {
        AtomicReference<Float> length = new AtomicReference<>(max);
        server.physics.rayCast((fixture, point, normal, fraction) -> {
            Object userData = fixture.getBody().getUserData();
            if(userData instanceof GameObject){
                if(((GameObject) userData).getLocalMode() == EObjectInteractionMode.Ghost){
                    return -1;
                }
            }
            if(length.get() > fraction * max)
                length.set(fraction * max);
            return fraction;
        }, getBaseBody().getPosition().cpy(), getBaseBody().getPosition().add(new Vector2(max, 0).setAngleRad((float) (getBaseBody().getAngle()+Math.PI/2))));
        return length.get();
    }

    @Override
    public void getAnimationData(ClientWorldManager.AnimationData animationData) {
        animationData.addNumber("length", getValueOnOutput(0));
        animationData.addNumber("max", max);
    }

    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("down", new ConnectionEdge(new Vector2(0, -0.15f), ConnectionEdgeType.Normal));
        return edges;
    }
}
