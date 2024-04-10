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
    public final ArrayList<SavedJoint> savedJoints;
    public SaveFile(HashMap<String, ArrayList<ArrayList<Vector2>>> terrain, ArrayList<SavedGameObject> savedGameObjects, ArrayList<SavedJoint> savedJoints) {
        this.terrain = terrain;
        this.savedGameObjects = savedGameObjects;
        this.savedJoints = savedJoints;
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
        this.savedJoints = new ArrayList<>();
        int savedJointCount = stream.readInt();
        for(int i = 0;i < savedJointCount;i++){
            this.savedJoints.add(new SavedJoint(stream));
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
        stream.writeInt(savedJoints.size());
        for(SavedJoint joint : savedJoints){
            joint.toStream(stream);
        }
    }
    public static class SavedGameObject{
        public final String type;
        public final UUID id;
        public final Vector2 position;
        public final float rotation;
        public final byte[] data;
        public SavedGameObject(String type, UUID id, Vector2 position, float rotation, byte[] data) {
            this.type = type;
            this.id = id;
            this.position = position;
            this.rotation = rotation;
            this.data = data;
        }
        public SavedGameObject(DataInputStream stream) throws IOException{
            this.type = stream.readUTF();
            this.id = new UUID(stream.readLong(), stream.readLong());
            this.position = new Vector2(stream.readFloat(), stream.readFloat());
            this.rotation = stream.readFloat();
            int dataLength = stream.readInt();
            this.data = stream.readNBytes(dataLength);
        }
        public void toStream(DataOutputStream stream) throws IOException{
            stream.writeUTF(type);
            stream.writeLong(id.getMostSignificantBits());
            stream.writeLong(id.getLeastSignificantBits());
            stream.writeFloat(position.x);
            stream.writeFloat(position.y);
            stream.writeFloat(rotation);
            stream.writeInt(data.length);
            stream.write(data);
        }
    }
    public static class SavedJoint{
        public final UUID first;
        public final String firstName;
        public final UUID second;
        public final String secondName;
        public SavedJoint(UUID first, String firstName, UUID second, String secondName) {
            this.first = first;
            this.firstName = firstName;
            this.second = second;
            this.secondName = secondName;
        }
        public SavedJoint(DataInputStream stream) throws IOException {
            this.first = new UUID(stream.readLong(), stream.readLong());
            this.firstName = stream.readUTF();
            this.second = new UUID(stream.readLong(), stream.readLong());
            this.secondName = stream.readUTF();
        }
        public void toStream(DataOutputStream stream) throws IOException {
            stream.writeLong(first.getMostSignificantBits());
            stream.writeLong(first.getLeastSignificantBits());
            stream.writeUTF(firstName);
            stream.writeLong(second.getMostSignificantBits());
            stream.writeLong(second.getLeastSignificantBits());
            stream.writeUTF(secondName);
        }
    }
}
