package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LockGameObject {
    public LockGameObject(){}
    public LockGameObject(DataInputStream stream) throws IOException {

    }
    public void toStream(DataOutputStream stream) throws IOException {

    }
    public static MessageRegistry.MessageDescriptor<LockGameObject> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(LockGameObject.class, LockGameObject::new, LockGameObject::toStream);
    }
}
