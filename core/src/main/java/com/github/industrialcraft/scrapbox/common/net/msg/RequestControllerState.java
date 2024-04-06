package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RequestControllerState {
    public final int gameObjectId;
    public RequestControllerState(int gameObjectId){
        this.gameObjectId = gameObjectId;
    }
    public RequestControllerState(DataInputStream stream) throws IOException {
        this.gameObjectId = stream.readInt();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(gameObjectId);
    }
    public static MessageRegistry.MessageDescriptor<RequestControllerState> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(RequestControllerState.class, RequestControllerState::new, RequestControllerState::toStream);
    }
}
