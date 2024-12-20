package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.netx.MessageRegistry;
import com.github.industrialcraft.scrapbox.server.ClientWorldManager;
import com.github.industrialcraft.scrapbox.server.GameObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class AddGameObjectMessage {
    public final int id;
    public final String type;
    public final Vector2 position;
    public final float rotation;
    public final ClientWorldManager.AnimationData animation;
    public final int selectionId;
    public final float maxHealth;
    public final float health;
    public final GameObject.GameObjectConfig config;
    public final boolean gearJoinable;
    public AddGameObjectMessage(int id, String type, Vector2 position, float rotation, ClientWorldManager.AnimationData animation, int selectionId, float maxHealth, float health, GameObject.GameObjectConfig config, boolean gearJoinable) {
        this.id = id;
        this.type = type;
        this.position = position;
        this.rotation = rotation;
        this.animation = animation;
        this.selectionId = selectionId;
        this.maxHealth = maxHealth;
        this.health = health;
        this.config = config;
        this.gearJoinable = gearJoinable;
    }
    public AddGameObjectMessage(DataInputStream stream) throws IOException {
        this.id = stream.readInt();
        this.type = stream.readUTF();
        this.position = new Vector2(stream.readFloat(), stream.readFloat());
        this.rotation = stream.readFloat();
        this.animation = new ClientWorldManager.AnimationData(stream);
        this.selectionId = stream.readInt();
        this.maxHealth = stream.readFloat();
        this.health = stream.readFloat();
        this.config = new GameObject.GameObjectConfig(stream);
        this.gearJoinable = stream.readBoolean();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(id);
        stream.writeUTF(type);
        stream.writeFloat(position.x);
        stream.writeFloat(position.y);
        stream.writeFloat(rotation);
        animation.toStream(stream);
        stream.writeInt(selectionId);
        stream.writeFloat(maxHealth);
        stream.writeFloat(health);
        config.toStream(stream);
        stream.writeBoolean(gearJoinable);
    }
    public static MessageRegistry.MessageDescriptor<AddGameObjectMessage> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(AddGameObjectMessage.class, AddGameObjectMessage::new, AddGameObjectMessage::toStream);
    }
}
