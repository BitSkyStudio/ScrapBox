package com.github.industrialcraft.scrapbox.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.github.industrialcraft.scrapbox.server.EDamageType;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.industrialcraft.scrapbox.server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class FireParticleGameObject extends GameObject {
    public int ttl;
    private boolean cancelled;
    public GameObject parent;
    public float damage;
    public FireParticleGameObject(Vector2 position, float rotation, Server server, GameObjectConfig config) {
        super(position, rotation, server, config);

        BodyDef bodyDef = new BodyDef();
        bodyDef.gravityScale = 0;
        bodyDef.bullet = true;
        bodyDef.position.set(position);
        bodyDef.angle = rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body base = server.physics.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(0.5f);
        fixtureDef.shape = shape;
        fixtureDef.density = 0.1F;
        fixtureDef.isSensor = true;
        base.createFixture(fixtureDef);
        this.setBody("base", "fire_particle", base, false);
        this.cancelled = false;
        this.ttl = Server.TPS;
        this.damage = 0;
    }

    @Override
    public void load(DataInputStream stream) throws IOException {
        super.load(stream);
        this.ttl = stream.readInt();
        this.damage = stream.readFloat();
    }

    @Override
    public void save(DataOutputStream stream) throws IOException {
        super.save(stream);
        stream.writeInt(ttl);
        stream.writeFloat(damage);
    }

    @Override
    public void tick() {
        super.tick();
        if(ttl <= 0 || cancelled){
            remove();
        }
        ttl--;
    }

    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        return new HashMap<>();
    }

    @Override
    public boolean collidesWith(Fixture thisFixture, Fixture other) {
        return !cancelled && other.getBody().getUserData() != parent && !(other.getBody().getUserData() instanceof FireParticleGameObject);
    }
    @Override
    public void onCollision(Fixture thisFixture, Fixture other, WorldManifold manifold) {
        if(cancelled)
            return;
        GameObject go = (GameObject) other.getBody().getUserData();
        if(go != null && (go.isRemoved() || go instanceof FireParticleGameObject))
            return;
        if(go != null) {
            go.damage(damage, EDamageType.Fire);
        }
        this.cancelled = true;
        remove();
    }
}
