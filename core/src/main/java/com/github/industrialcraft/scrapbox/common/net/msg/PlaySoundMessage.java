package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlaySoundMessage {
    public final int id;
    public final String sound;
    public final int gameObjectId;
    public final Vector2 offset;
    public final boolean loop;
    public final float volume;
    public PlaySoundMessage(int id, String sound, int gameObjectId, Vector2 offset, boolean loop, float volume) {
        this.id = id;
        this.sound = sound;
        this.gameObjectId = gameObjectId;
        this.offset = offset;
        this.loop = loop;
        this.volume = volume;
    }
    public PlaySoundMessage(DataInputStream stream) throws IOException {
        this.id = stream.readInt();
        this.sound = stream.readUTF();
        this.gameObjectId = stream.readInt();
        this.offset = new Vector2(stream.readFloat(), stream.readFloat());
        this.loop = stream.readBoolean();
        this.volume = stream.readFloat();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(id);
        stream.writeUTF(sound);
        stream.writeInt(gameObjectId);
        stream.writeFloat(offset.x);
        stream.writeFloat(offset.y);
        stream.writeBoolean(loop);
        stream.writeFloat(volume);
    }
    public static MessageRegistry.MessageDescriptor<PlaySoundMessage> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(PlaySoundMessage.class, PlaySoundMessage::new, PlaySoundMessage::toStream);
    }
}
