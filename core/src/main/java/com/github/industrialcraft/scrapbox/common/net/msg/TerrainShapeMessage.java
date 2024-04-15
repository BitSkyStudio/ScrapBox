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
    public final HashMap<String,ArrayList<TerrainData>> terrain;
    public TerrainShapeMessage(HashMap<String,ArrayList<TerrainData>> terrain) {
        this.terrain = terrain;
    }
    public TerrainShapeMessage(DataInputStream stream) throws IOException {
        int count = stream.readInt();
        this.terrain = new HashMap<>();
        for(int i = 0;i < count;i++){
            String name = stream.readUTF();
            int count2 = stream.readInt();
            ArrayList<TerrainData> terrainData = new ArrayList<>();
            for(int j = 0;j < count2;j++)
                terrainData.add(new TerrainData(stream));
            this.terrain.put(name,terrainData);
        }
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(terrain.size());
        for(Map.Entry<String, ArrayList<TerrainData>> path : terrain.entrySet()){
            stream.writeUTF(path.getKey());
            stream.writeInt(path.getValue().size());
            for (TerrainData terrainData : path.getValue()) {
                terrainData.toStream(stream);
            }
        }
    }
    public static MessageRegistry.MessageDescriptor<TerrainShapeMessage> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(TerrainShapeMessage.class, TerrainShapeMessage::new, TerrainShapeMessage::toStream);
    }
    public static class TerrainData{
        public final TerrainPath terrain;
        public final ArrayList<TerrainPath> holes;
        public TerrainData(TerrainPath terrain, ArrayList<TerrainPath> holes) {
            this.terrain = terrain;
            this.holes = holes;
        }
        public TerrainData(DataInputStream stream) throws IOException {
            this.terrain = new TerrainPath(stream);
            int holesCount = stream.readInt();
            this.holes = new ArrayList<>(holesCount);
            for(int i = 0;i < holesCount;i++){
                holes.add(new TerrainPath(stream));
            }
        }
        public void toStream(DataOutputStream stream) throws IOException {
            terrain.toStream(stream);
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
