package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ResponseControllerState {
    public final boolean[] state;
    public ResponseControllerState(boolean[] state) {
        this.state = state;
    }
    public ResponseControllerState(DataInputStream stream) throws IOException {
        this.state = new boolean[10];
        for(int i = 0;i < 10;i++){
            this.state[i] = stream.readBoolean();
        }
    }
    public void toStream(DataOutputStream stream) throws IOException {
        for (int i = 0; i < 10; i++) {
            stream.writeBoolean(state[i]);
        }
    }
    public static MessageRegistry.MessageDescriptor<ResponseControllerState> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(ResponseControllerState.class, ResponseControllerState::new, ResponseControllerState::toStream);
    }
}
