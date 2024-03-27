package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ShowActivePossibleWelds {
    public final ArrayList<PossibleWeld> welds;
    public ShowActivePossibleWelds(ArrayList<PossibleWeld> welds) {
        this.welds = welds;
    }
    public static class PossibleWeld{
        public final Vector2 first;
        public final Vector2 second;
        public PossibleWeld(Vector2 first, Vector2 second) {
            this.first = first;
            this.second = second;
        }
    }
    public ShowActivePossibleWelds(DataInputStream stream) throws IOException {
        int count = stream.readInt();
        this.welds = new ArrayList<>(count);
        for(int i = 0;i < count;i++){
            this.welds.add(new PossibleWeld(new Vector2(stream.readFloat(), stream.readFloat()), new Vector2(stream.readFloat(), stream.readFloat())));
        }
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(this.welds.size());
        for(PossibleWeld weld : this.welds){
            stream.writeFloat(weld.first.x);
            stream.writeFloat(weld.first.y);
            stream.writeFloat(weld.second.x);
            stream.writeFloat(weld.second.y);
        }
    }
    public static MessageRegistry.MessageDescriptor<ShowActivePossibleWelds> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(ShowActivePossibleWelds.class, ShowActivePossibleWelds::new, ShowActivePossibleWelds::toStream);
    }
}
