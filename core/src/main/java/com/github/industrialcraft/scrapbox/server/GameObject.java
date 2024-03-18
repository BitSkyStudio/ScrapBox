package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.github.industrialcraft.scrapbox.common.net.MessageS2C;
import com.github.industrialcraft.scrapbox.common.net.msg.AddGameObjectMessage;
import com.github.industrialcraft.scrapbox.common.net.msg.MoveGameObjectMessage;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class GameObject {
    private static final AtomicInteger idGenerator = new AtomicInteger(0);

    public final Server server;
    public final int id;
    public final Body body;
    private boolean isRemoved;
    protected GameObject(Vector2 position, Server server){
        this.server = server;
        this.id = idGenerator.addAndGet(1);
        this.body = server.physics.createBody(create_body_def(position));
        this.add_fixtures();
        this.isRemoved = false;
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
    protected abstract void add_fixtures();
    public abstract String get_type();

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
}
