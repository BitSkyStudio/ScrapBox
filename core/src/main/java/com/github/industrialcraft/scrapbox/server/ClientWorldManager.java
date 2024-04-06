package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.physics.box2d.Body;
import com.github.industrialcraft.scrapbox.common.net.msg.AddGameObjectMessage;
import com.github.industrialcraft.scrapbox.common.net.msg.DeleteGameObject;
import com.github.industrialcraft.scrapbox.common.net.msg.MoveGameObjectMessage;
import com.github.industrialcraft.scrapbox.common.net.msg.SendConnectionListData;

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
        server.players.forEach(player -> player.send(bodyInfo.createAddMessage()));
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
        this.bodies.forEach(bodyInfo -> player.send(bodyInfo.createAddMessage()));
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
            return new AddGameObjectMessage(this.id, this.type, this.body.getPosition().cpy(), this.body.getAngle(), selectable);
        }
        public MoveGameObjectMessage createMoveMessage(Player player){
            GameObject pinching = player.getPinching();
            boolean selected = false;
            if(pinching != null){
                if(pinching.vehicle == this.gameObject.vehicle){
                    selected = true;
                }
            }
            return new MoveGameObjectMessage(this.id, this.body.getPosition().cpy(), this.body.getAngle(), gameObject.vehicle.getMode(), selected);
        }
    }
}
