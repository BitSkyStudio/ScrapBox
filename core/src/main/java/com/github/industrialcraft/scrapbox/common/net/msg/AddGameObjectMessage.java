package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.netx.MessageRegistry;
import com.github.industrialcraft.scrapbox.server.ClientWorldManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class AddGameObjectMessage {
    public final int id;
    public final String type;
    public final Vector2 position;
    public final float rotation;
    public final ClientWorldManager.AnimationData animation;
    public final boolean selectable;
    public AddGameObjectMessage(int id, String type, Vector2 position, float rotation, ClientWorldManager.AnimationData animation, boolean selectable) {
        this.id = id;
        this.type = type;
        this.position = position;
        this.rotation = rotation;
        this.animation = animation;
        this.selectable = selectable;
    }
    public AddGameObjectMessage(DataInputStream stream) throws IOException {
        this.id = stream.readInt();
        this.type = stream.readUTF();
        this.position = new Vector2(stream.readFloat(), stream.readFloat());
        this.rotation = stream.readFloat();
        this.animation = new ClientWorldManager.AnimationData(stream);
        this.selectable = stream.readBoolean();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(id);
        stream.writeUTF(type);
        stream.writeFloat(position.x);
        stream.writeFloat(position.y);
        stream.writeFloat(rotation);
        animation.toStream(stream);
        stream.writeBoolean(selectable);
    }
    public static MessageRegistry.MessageDescriptor<AddGameObjectMessage> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(AddGameObjectMessage.class, AddGameObjectMessage::new, AddGameObjectMessage::toStream);
    }
}
