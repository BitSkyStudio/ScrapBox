package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class EditorUIInput {
    public final int gameObjectId;
    public final String elementId;
    public final String value;
    public EditorUIInput(int gameObjectId, String elementId, String value) {
        this.gameObjectId = gameObjectId;
        this.elementId = elementId;
        this.value = value;
    }
    public EditorUIInput(DataInputStream stream) throws IOException {
        this.gameObjectId = stream.readInt();
        this.elementId = stream.readUTF();
        this.value = stream.readUTF();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(gameObjectId);
        stream.writeUTF(elementId);
        stream.writeUTF(value);
    }
    public static MessageRegistry.MessageDescriptor<EditorUIInput> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(EditorUIInput.class, EditorUIInput::new, EditorUIInput::toStream);
    }
}
