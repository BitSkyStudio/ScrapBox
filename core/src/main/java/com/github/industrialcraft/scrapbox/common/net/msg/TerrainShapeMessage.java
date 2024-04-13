package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TerrainShapeMessage {
    public final HashMap<String,TerrainData> terrain;
    public TerrainShapeMessage(HashMap<String,TerrainData> terrain) {
        this.terrain = terrain;
    }
    public TerrainShapeMessage(DataInputStream stream) throws IOException {
        int count = stream.readInt();
        this.terrain = new HashMap<>();
        for(int i = 0;i < count;i++){
            this.terrain.put(stream.readUTF(), new TerrainData(stream));
        }
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(terrain.size());
        for(Map.Entry<String, TerrainData> path : terrain.entrySet()){
            stream.writeUTF(path.getKey());
            path.getValue().toStream(stream);
        }
    }
    public static MessageRegistry.MessageDescriptor<TerrainShapeMessage> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(TerrainShapeMessage.class, TerrainShapeMessage::new, TerrainShapeMessage::toStream);
    }
    public static class TerrainData{
        public final ArrayList<TerrainPath> terrain;
        public final ArrayList<TerrainPath> holes;
        public TerrainData(ArrayList<TerrainPath> terrain, ArrayList<TerrainPath> holes) {
            this.terrain = terrain;
            this.holes = holes;
        }
        public TerrainData(DataInputStream stream) throws IOException {
            int terrainCount = stream.readInt();
            this.terrain = new ArrayList<>(terrainCount);
            for(int i = 0;i < terrainCount;i++){
                terrain.add(new TerrainPath(stream));
            }
            int holesCount = stream.readInt();
            this.holes = new ArrayList<>(holesCount);
            for(int i = 0;i < holesCount;i++){
                holes.add(new TerrainPath(stream));
            }
        }
        public void toStream(DataOutputStream stream) throws IOException {
            stream.writeInt(this.terrain.size());
            for(TerrainPath path : this.terrain){
                path.toStream(stream);
            }
            stream.writeInt(this.holes.size());
            for(TerrainPath path : this.holes){
                path.toStream(stream);
            }
        }
    }
    public static class TerrainPath{
        public final ArrayList<Vector2> points;
        public TerrainPath(ArrayList<Vector2> points) {
            this.points = points;
        }
        public TerrainPath(DataInputStream stream) throws IOException {
            int count = stream.readInt();
            this.points = new ArrayList<>(count);
            for(int i = 0;i < count;i++){
                this.points.add(new Vector2(stream.readFloat(), stream.readFloat()));
            }
        }
        public void toStream(DataOutputStream stream) throws IOException {
            stream.writeInt(this.points.size());
            for(Vector2 point : points){
                stream.writeFloat(point.x);
                stream.writeFloat(point.y);
            }
        }
    }
}
