package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PinchingSetGhost {
    public final boolean isGhost;
    public PinchingSetGhost(boolean isGhost) {
        this.isGhost = isGhost;
    }
    public PinchingSetGhost(DataInputStream stream) throws IOException {
        this.isGhost = stream.readBoolean();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeBoolean(isGhost);
    }
    public static MessageRegistry.MessageDescriptor<PinchingSetGhost> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(PinchingSetGhost.class, PinchingSetGhost::new, PinchingSetGhost::toStream);
    }
}
