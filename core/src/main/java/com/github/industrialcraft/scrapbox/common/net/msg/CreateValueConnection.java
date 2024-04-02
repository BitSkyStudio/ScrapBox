package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CreateValueConnection {
    public final int inputObjectId;
    public final int inputId;
    public final int outputObjectId;
    public final int outputId;
    public CreateValueConnection(int inputObjectId, int inputId, int outputObjectId, int outputId) {
        this.inputObjectId = inputObjectId;
        this.inputId = inputId;
        this.outputObjectId = outputObjectId;
        this.outputId = outputId;
    }
    public CreateValueConnection(DataInputStream stream) throws IOException {
        this.inputObjectId = stream.readInt();
        this.inputId = stream.readInt();
        this.outputObjectId = stream.readInt();
        this.outputId = stream.readInt();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(inputObjectId);
        stream.writeInt(inputId);
        stream.writeInt(outputObjectId);
        stream.writeInt(outputId);
    }
    public static MessageRegistry.MessageDescriptor<CreateValueConnection> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(CreateValueConnection.class, CreateValueConnection::new, CreateValueConnection::toStream);
    }
}
