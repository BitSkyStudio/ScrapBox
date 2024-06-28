package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.github.industrialcraft.scrapbox.common.editui.*;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class DistanceSensorGameObject extends GameObject {
    public DistanceSensorGameObject(Vector2 position, float rotation, Server server) {
        super(position, rotation, server);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f, 0.25f);
        fixtureDef.shape = shape;
        fixtureDef.density = 1F;
        base.createFixture(fixtureDef);
        this.setBody("base", "distance_sensor", base);
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
        float max = 10;
        AtomicReference<Float> length = new AtomicReference<>(max);
        server.physics.rayCast((fixture, point, normal, fraction) -> {
            if(length.get() > fraction * max)
                length.set(fraction * max);
            return fraction;
        }, getBaseBody().getPosition().cpy(), getBaseBody().getPosition().add(new Vector2(max, 0).setAngleRad((float) (getBaseBody().getAngle()+Math.PI/2))));
        return length.get();
    }

    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        HashMap<String, ConnectionEdge> edges = new HashMap<>();
        edges.put("down", new ConnectionEdge(new Vector2(0, -0.25f), false));
        return edges;
    }

    @Override
    public String getType() {
        return "distance_sensor";
    }
}
