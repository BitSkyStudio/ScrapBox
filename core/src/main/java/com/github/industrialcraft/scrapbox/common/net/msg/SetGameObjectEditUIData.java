package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SetGameObjectEditUIData {
    public final int id;
    public final String text;
    public SetGameObjectEditUIData(int id, String text) {
        this.id = id;
        this.text = text;
    }
    public SetGameObjectEditUIData(DataInputStream stream) throws IOException {
        this.id = stream.readInt();
        this.text = stream.readUTF();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(id);
        stream.writeUTF(text);
    }
    public static MessageRegistry.MessageDescriptor<SetGameObjectEditUIData> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(SetGameObjectEditUIData.class, SetGameObjectEditUIData::new, SetGameObjectEditUIData::toStream);
    }
}
