package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.netx.MessageRegistry;
import com.github.industrialcraft.scrapbox.common.EObjectInteractionMode;
import com.github.industrialcraft.scrapbox.server.ClientWorldManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MoveGameObjectMessage {
    public final int id;
    public final Vector2 position;
    public final float rotation;
    public final EObjectInteractionMode mode;
    public final ClientWorldManager.AnimationData animation;
    public final boolean selected;
    public final float health;
    public MoveGameObjectMessage(int id, Vector2 position, float rotation, EObjectInteractionMode mode, ClientWorldManager.AnimationData animation, boolean selected, float health) {
        this.id = id;
        this.position = position;
        this.rotation = rotation;
        this.mode = mode;
        this.animation = animation;
        this.selected = selected;
        this.health = health;
    }
    public MoveGameObjectMessage(DataInputStream stream) throws IOException {
        this.id = stream.readInt();
        this.position = new Vector2(stream.readFloat(), stream.readFloat());
        this.rotation = stream.readFloat();
        this.mode = EObjectInteractionMode.fromId(stream.readByte());
        this.animation = new ClientWorldManager.AnimationData(stream);
        this.selected = stream.readBoolean();
        this.health = stream.readFloat();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(id);
        stream.writeFloat(position.x);
        stream.writeFloat(position.y);
        stream.writeFloat(rotation);
        animation.toStream(stream);
        stream.writeBoolean(selected);
        stream.writeFloat(health);
    }
    public static MessageRegistry.MessageDescriptor<MoveGameObjectMessage> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(MoveGameObjectMessage.class, MoveGameObjectMessage::new, MoveGameObjectMessage::toStream);
    }
}
