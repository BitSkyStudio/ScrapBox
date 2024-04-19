package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DestroyValueConnection {
    public final int inputObjectId;
    public final int inputId;
    public DestroyValueConnection(int inputObjectId, int inputId) {
        this.inputObjectId = inputObjectId;
        this.inputId = inputId;
    }
    public DestroyValueConnection(DataInputStream stream) throws IOException {
        this.inputObjectId = stream.readInt();
        this.inputId = stream.readInt();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(inputObjectId);
        stream.writeInt(inputId);
    }
    public static MessageRegistry.MessageDescriptor<DestroyValueConnection> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(DestroyValueConnection.class, DestroyValueConnection::new, DestroyValueConnection::toStream);
    }
}
