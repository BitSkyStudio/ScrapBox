package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SetGameState {
    public final GameState gameState;
    public SetGameState(GameState gameState){
        this.gameState = gameState;
    }
    public SetGameState(DataInputStream stream) throws IOException {
        byte gameState = stream.readByte();
        if(gameState == 0)
            this.gameState = GameState.REQUEST_PASSWORD;
        else
            this.gameState = GameState.PLAY;
    }
    public void toStream(DataOutputStream stream) throws IOException {
        switch (gameState){
            case REQUEST_PASSWORD:
                stream.writeByte(0);
                break;
            case PLAY:
                stream.writeByte(1);
                break;
        }
    }
    public static MessageRegistry.MessageDescriptor<SetGameState> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(SetGameState.class, SetGameState::new, SetGameState::toStream);
    }
    public enum GameState{
        REQUEST_PASSWORD,
        PLAY;
    }
}
