package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CommitWeld {
    public CommitWeld(){}
    public CommitWeld(DataInputStream stream) throws IOException {

    }
    public void toStream(DataOutputStream stream) throws IOException {

    }
    public static MessageRegistry.MessageDescriptor<CommitWeld> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(CommitWeld.class, CommitWeld::new, CommitWeld::toStream);
    }
}
