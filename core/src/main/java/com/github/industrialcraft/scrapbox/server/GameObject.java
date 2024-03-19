package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.github.industrialcraft.scrapbox.common.net.MessageS2C;
import com.github.industrialcraft.scrapbox.common.net.msg.AddGameObjectMessage;
import com.github.industrialcraft.scrapbox.common.net.msg.MoveGameObjectMessage;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class GameObject {
    private static final AtomicInteger idGenerator = new AtomicInteger(0);

    public final Server server;
    public final int id;
    public final Body body;
    private boolean isRemoved;
    private HashMap<String,GameObject> connections;
    protected GameObject(Vector2 position, Server server){
        this.server = server;
        this.id = idGenerator.addAndGet(1);
        this.body = server.physics.createBody(create_body_def(position));
        this.body.setUserData(this);
        this.add_fixtures();
        this.isRemoved = false;
        this.connections = new HashMap<>();
    }
    protected BodyDef create_body_def(Vector2 position){
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        return bodyDef;
    }
    public void remove(){
        if(!isRemoved){
            server.physics.destroyBody(this.body);
        }
        this.isRemoved = true;
    }
    public boolean isRemoved(){
        return this.isRemoved;
    }
    public void tick(){
        if(this.body.getPosition().y < -100){
            remove();
        }
    }

    protected abstract void add_fixtures();
    public abstract String get_type();

    public abstract HashMap<String,ConnectionEdge> getConnectionEdges();

    public void setGhost(boolean isGhost){
        Filter filter = new Filter();
        if(isGhost){
            filter.categoryBits = 0;
            filter.maskBits = 0;
        }
        this.body.getFixtureList().forEach(fixture -> fixture.setFilterData(filter));
    }
    public void setRotatable(boolean isRotatable){
        this.body.setFixedRotation(!isRotatable);
    }

    public MessageS2C create_add_message(){
        return new AddGameObjectMessage(this.id, this.get_type(), this.body.getPosition(), this.body.getAngle());
    }
    public MessageS2C create_move_message(){
        return new MoveGameObjectMessage(this.id, this.body.getPosition(), this.body.getAngle());
    }
    @FunctionalInterface
    public interface GameObjectSpawner<T extends GameObject>{
        T spawn(Vector2 position, Server server);
    }
    public static class ConnectionEdge{
        public final Vector2 offset;
        public final Vector2 normal;
        public ConnectionEdge(Vector2 offset, Vector2 normal) {
            this.offset = offset;
            this.normal = normal;
        }
    }
}
