package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class OpenGameObjectEditUI {
    public final int id;
    public OpenGameObjectEditUI(int id) {
        this.id = id;
    }
    public OpenGameObjectEditUI(DataInputStream stream) throws IOException {
        this.id = stream.readInt();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(id);
    }
    public static MessageRegistry.MessageDescriptor<OpenGameObjectEditUI> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(OpenGameObjectEditUI.class, OpenGameObjectEditUI::new, OpenGameObjectEditUI::toStream);
    }
}
