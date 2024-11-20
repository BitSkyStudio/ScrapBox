package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StopSoundMessage {
    public final int id;
    public StopSoundMessage(int id) {
        this.id = id;
    }
    public StopSoundMessage(DataInputStream stream) throws IOException {
        this.id = stream.readInt();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(id);
    }
    public static MessageRegistry.MessageDescriptor<StopSoundMessage> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(StopSoundMessage.class, StopSoundMessage::new, StopSoundMessage::toStream);
    }
}
