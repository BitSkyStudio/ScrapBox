package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DisconnectMessage {
    public final String reason;
    public DisconnectMessage(String reason){
        this.reason = reason;
    }
    public DisconnectMessage(DataInputStream stream) throws IOException {
        this.reason = stream.readUTF();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeUTF(reason);
    }
    public static MessageRegistry.MessageDescriptor<DisconnectMessage> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(DisconnectMessage.class, DisconnectMessage::new, DisconnectMessage::toStream);
    }
}
