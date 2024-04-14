package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class BulletGameObject extends GameObject {
    public GameObject parent;
    private int ttl;
    public BulletGameObject(Vector2 position, float rotation, Server server) {
        super(position, rotation, server);

        BodyDef bodyDef = new BodyDef();
        bodyDef.gravityScale = 0;
        bodyDef.bullet = true;
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(0.1f);
        fixtureDef.shape = shape;
        fixtureDef.density = 1F;
        base.createFixture(fixtureDef);
        this.setBody("base", "bullet", base);

        this.ttl = 100;
    }

    @Override
    public void load(DataInputStream stream) throws IOException {
        super.load(stream);
        if(stream.readBoolean())
            this.parent = server.getGameObjectByUUID(new UUID(stream.readLong(), stream.readLong()));
    }

    @Override
    public void save(DataOutputStream stream) throws IOException {
        super.save(stream);
        stream.writeBoolean(parent != null);
        if(parent != null) {
            stream.writeLong(parent.uuid.getMostSignificantBits());
            stream.writeLong(parent.uuid.getLeastSignificantBits());
        }
    }

    @Override
    public void tick() {
        super.tick();
        if(ttl <= 0){
            remove();
        }
        ttl--;
    }

    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        return new HashMap<>();
    }

    @Override
    public boolean collidesWith(Body thisBody, Body other) {
        if(other.getUserData() != parent){
            this.remove();
            server.createExplosion(getBaseBody().getPosition(), 0.5f);
            return true;
        } else {
            return false;
        }
    }
    @Override
    public String getType() {
        return "bullet";
    }
}
