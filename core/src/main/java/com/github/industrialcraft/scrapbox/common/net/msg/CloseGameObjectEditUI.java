package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CloseGameObjectEditUI {
    public final int id;
    public CloseGameObjectEditUI(int id) {
        this.id = id;
    }
    public CloseGameObjectEditUI(DataInputStream stream) throws IOException {
        this.id = stream.readInt();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(id);
    }
    public static MessageRegistry.MessageDescriptor<CloseGameObjectEditUI> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(CloseGameObjectEditUI.class, CloseGameObjectEditUI::new, CloseGameObjectEditUI::toStream);
    }
}
