package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class GameObjectRelease {
    public GameObjectRelease(){}
    public GameObjectRelease(DataInputStream stream) {

    }
    public void toStream(DataOutputStream stream) throws IOException {

    }
    public static MessageRegistry.MessageDescriptor<GameObjectRelease> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(GameObjectRelease.class, GameObjectRelease::new, GameObjectRelease::toStream);
    }
}
