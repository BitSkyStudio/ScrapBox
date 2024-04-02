package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PinchingRotate {
    public final float rotation;
    public PinchingRotate(float rotation) {
        this.rotation = rotation;
    }
    public PinchingRotate(DataInputStream stream) throws IOException {
        this.rotation = stream.readFloat();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeFloat(rotation);
    }
    public static MessageRegistry.MessageDescriptor<PinchingRotate> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(PinchingRotate.class, PinchingRotate::new, PinchingRotate::toStream);
    }
}
