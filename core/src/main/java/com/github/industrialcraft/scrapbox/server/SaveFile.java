package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.math.Vector2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SaveFile {
    public final HashMap<String, ArrayList<ArrayList<Vector2>>> terrain;
    public final ArrayList<SavedGameObject> savedGameObjects;
    public SaveFile(HashMap<String, ArrayList<ArrayList<Vector2>>> terrain, ArrayList<SavedGameObject> savedGameObjects) {
        this.terrain = terrain;
        this.savedGameObjects = savedGameObjects;
    }
    public SaveFile(DataInputStream stream) throws IOException {
        this.terrain = new HashMap<>();
        int terrainTypesCount = stream.readInt();
        for(int i = 0;i < terrainTypesCount;i++){
            String type = stream.readUTF();
            ArrayList<ArrayList<Vector2>> terrainType = new ArrayList<>();
            int pathsCount = stream.readInt();
            for(int j = 0;j < pathsCount;j++){
                ArrayList<Vector2> path = new ArrayList<>();
                int pathLength = stream.readInt();
                for(int k = 0;k < pathLength;k++){
                    path.add(new Vector2(stream.readFloat(), stream.readFloat()));
                }
                terrainType.add(path);
            }
            this.terrain.put(type, terrainType);
        }
        this.savedGameObjects = new ArrayList<>();
        int savedGameObjectCount = stream.readInt();
        for(int i = 0;i < savedGameObjectCount;i++){
            this.savedGameObjects.add(new SavedGameObject(stream));
        }
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(terrain.size());
        for(Map.Entry<String, ArrayList<ArrayList<Vector2>>> entry : terrain.entrySet()){
            stream.writeUTF(entry.getKey());
            stream.writeInt(entry.getValue().size());
            for(ArrayList<Vector2> path : entry.getValue()){
                stream.writeInt(path.size());
                for(Vector2 point : path){
                    stream.writeFloat(point.x);
                    stream.writeFloat(point.y);
                }
            }
        }
        stream.writeInt(savedGameObjects.size());
        for(SavedGameObject gameObject : savedGameObjects){
            gameObject.toStream(stream);
        }
    }
    public static class SavedGameObject{
        public final String type;
        public final UUID id;
        public final Vector2 position;
        public final float rotation;
        public SavedGameObject(String type, UUID id, Vector2 position, float rotation) {
            this.type = type;
            this.id = id;
            this.position = position;
            this.rotation = rotation;
        }
        public SavedGameObject(DataInputStream stream) throws IOException{
            this.type = stream.readUTF();
            this.id = new UUID(stream.readLong(), stream.readLong());
            this.position = new Vector2(stream.readFloat(), stream.readFloat());
            this.rotation = stream.readFloat();
        }
        public void toStream(DataOutputStream stream) throws IOException{
            stream.writeUTF(type);
            stream.writeLong(id.getMostSignificantBits());
            stream.writeLong(id.getLeastSignificantBits());
            stream.writeFloat(position.x);
            stream.writeFloat(position.y);
            stream.writeFloat(rotation);
        }
    }
}
