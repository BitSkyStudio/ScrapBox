package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.physics.box2d.Body;
import com.github.industrialcraft.scrapbox.common.net.msg.AddGameObjectMessage;
import com.github.industrialcraft.scrapbox.common.net.msg.DeleteGameObject;
import com.github.industrialcraft.scrapbox.common.net.msg.MoveGameObjectMessage;
import com.github.industrialcraft.scrapbox.common.net.msg.SendConnectionListData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClientWorldManager {
    public final Server server;
    private int bodyIdGenerator;
    private final ArrayList<BodyInfo> bodies;
    public ClientWorldManager(Server server) {
        this.server = server;
        this.bodyIdGenerator = 0;
        this.bodies = new ArrayList<>();
    }
    public int addBody(GameObject gameObject, Body body, String type, boolean selectable){
        BodyInfo bodyInfo = new BodyInfo(body, type, gameObject, ++this.bodyIdGenerator, selectable);
        this.bodies.add(bodyInfo);
        server.players.forEach(player -> {
            if(player != gameObject) player.send(bodyInfo.createAddMessage());
        });
        return bodyInfo.id;
    }
    public void removeObject(GameObject gameObject){
        this.bodies.removeIf(bodyInfo -> {
            if(bodyInfo.gameObject == gameObject){
                server.players.forEach(player -> player.send(new DeleteGameObject(bodyInfo.id)));
                return true;
            }
            return false;
        });
    }
    public void addPlayer(Player player){
        this.bodies.forEach(bodyInfo -> {
            if(bodyInfo.gameObject != player) player.send(bodyInfo.createAddMessage());
        });
    }
    public void updatePositions(){
        this.bodies.forEach(bodyInfo -> server.players.forEach(player -> player.send(bodyInfo.createMoveMessage(player))));

        ArrayList<SendConnectionListData.Connection> connections = new ArrayList<>();
        for(GameObject gameObject : server.gameObjects.values()){
            HashMap<String, GameObject.ConnectionEdge> connectionPositions = gameObject.getConnectionEdges();
            for(Map.Entry<String, GameObject.ConnectionData> connection : gameObject.connections.entrySet()){
                if(gameObject.getId() < connection.getValue().other.getId()) {
                    connections.add(new SendConnectionListData.Connection(gameObject.getBaseBody().getWorldPoint(connectionPositions.get(connection.getKey()).offset).cpy(), gameObject.getId(), connection.getKey()));
                }
            }
        }
        SendConnectionListData connectionListData = new SendConnectionListData(connections);
        server.players.forEach(player -> player.send(connectionListData));
    }

    private static class BodyInfo{
        public final Body body;
        public final String type;
        public final GameObject gameObject;
        public final int id;
        public final boolean selectable;
        private BodyInfo(Body body, String type, GameObject gameObject, int id, boolean selectable) {
            this.body = body;
            this.type = type;
            this.gameObject = gameObject;
            this.id = id;
            this.selectable = selectable;
        }
        public AddGameObjectMessage createAddMessage(){
            AnimationData animationData = new AnimationData();
            gameObject.getAnimationData(animationData);
            return new AddGameObjectMessage(this.id, this.type, this.body.getPosition().cpy(), this.body.getAngle(), animationData, selectable, gameObject.health);
        }
        public MoveGameObjectMessage createMoveMessage(Player player){
            GameObject pinching = player.getPinching();
            boolean selected = false;
            if(pinching != null){
                if(pinching.vehicle == this.gameObject.vehicle){
                    selected = true;
                }
            }
            AnimationData animationData = new AnimationData();
            gameObject.getAnimationData(animationData);
            return new MoveGameObjectMessage(this.id, this.body.getPosition().cpy(), this.body.getAngle(), gameObject.getLocalMode(), animationData, selected, gameObject.health);
        }
    }
    public static class AnimationData{
        private HashMap<String, Float> numbers;
        private HashMap<String, String> strings;
        public AnimationData(){
            this.numbers = new HashMap<>();
            this.strings = new HashMap<>();
        }
        public AnimationData(DataInputStream stream) throws IOException {
            this.numbers = new HashMap<>();
            int count = stream.readInt();
            for(int i = 0;i < count;i++){
                this.numbers.put(stream.readUTF(), stream.readFloat());
            }
            this.strings = new HashMap<>();
            count = stream.readInt();
            for(int i = 0;i < count;i++){
                this.strings.put(stream.readUTF(), stream.readUTF());
            }
        }
        public void toStream(DataOutputStream stream) throws IOException {
            stream.writeInt(numbers.size());
            for(Map.Entry<String, Float> entry : numbers.entrySet()){
                stream.writeUTF(entry.getKey());
                stream.writeFloat(entry.getValue());
            }
            stream.writeInt(strings.size());
            for(Map.Entry<String, String> entry : strings.entrySet()){
                stream.writeUTF(entry.getKey());
                stream.writeUTF(entry.getValue());
            }
        }
        public void addNumber(String name, float number){
            this.numbers.put(name, number);
        }
        public void addString(String name, String text){
            this.strings.put(name, text);
        }
        public float getNumber(String name, float defaultNumber){
            return this.numbers.getOrDefault(name, defaultNumber);
        }
        public String getString(String name, String defaultString){
            return this.strings.getOrDefault(name, defaultString);
        }
    }
}
