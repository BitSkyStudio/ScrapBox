package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DeleteGameObject {
    public final int id;
    public DeleteGameObject(int id) {
        this.id = id;
    }
    public DeleteGameObject(DataInputStream stream) throws IOException {
        this.id = stream.readInt();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(id);
    }
    public static MessageRegistry.MessageDescriptor<DeleteGameObject> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(DeleteGameObject.class, DeleteGameObject::new, DeleteGameObject::toStream);
    }
}
