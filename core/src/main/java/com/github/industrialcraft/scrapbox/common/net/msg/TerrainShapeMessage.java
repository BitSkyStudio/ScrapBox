package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class TerrainShapeMessage {
    public final ArrayList<ArrayList<Vector2>> terrain;
    public TerrainShapeMessage(ArrayList<ArrayList<Vector2>> terrain) {
        this.terrain = terrain;
    }
    public TerrainShapeMessage(DataInputStream stream) throws IOException {
        int count = stream.readInt();
        this.terrain = new ArrayList<>(count);
        for(int i = 0;i < count;i++){
            int length = stream.readInt();
            ArrayList<Vector2> path = new ArrayList<>(length);
            for(int j = 0;j < length;j++){
                path.add(new Vector2(stream.readFloat(), stream.readFloat()));
            }
            this.terrain.add(path);
        }
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(terrain.size());
        for(ArrayList<Vector2> path : terrain){
            stream.writeInt(path.size());
            for(Vector2 point : path){
                stream.writeFloat(point.x);
                stream.writeFloat(point.y);
            }
        }
    }
    public static MessageRegistry.MessageDescriptor<TerrainShapeMessage> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(TerrainShapeMessage.class, TerrainShapeMessage::new, TerrainShapeMessage::toStream);
    }
}
