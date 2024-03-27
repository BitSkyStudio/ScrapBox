package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TrashObject {
    public final int id;
    public TrashObject(int id) {
        this.id = id;
    }
    public TrashObject(DataInputStream stream) throws IOException {
        this.id = stream.readInt();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(id);
    }
    public static MessageRegistry.MessageDescriptor<TrashObject> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(TrashObject.class, TrashObject::new, TrashObject::toStream);
    }
}
