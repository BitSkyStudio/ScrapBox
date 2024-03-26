package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.physics.box2d.Body;
import com.github.industrialcraft.scrapbox.common.net.msg.AddGameObjectMessage;
import com.github.industrialcraft.scrapbox.common.net.msg.DeleteGameObject;
import com.github.industrialcraft.scrapbox.common.net.msg.MoveGameObjectMessage;

import java.util.ArrayList;

public class ClientWorldManager {
    public final Server server;
    private int bodyIdGenerator;
    private final ArrayList<BodyInfo> bodies;
    public ClientWorldManager(Server server) {
        this.server = server;
        this.bodyIdGenerator = 0;
        this.bodies = new ArrayList<>();
    }
    public void addBody(GameObject gameObject, Body body, String type){
        BodyInfo bodyInfo = new BodyInfo(body, type, gameObject, ++this.bodyIdGenerator);
        this.bodies.add(bodyInfo);
        server.players.forEach(player -> player.send(bodyInfo.createAddMessage()));
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
        this.bodies.forEach(bodyInfo -> server.players.forEach(player -> player.send(bodyInfo.createMoveMessage())));
    }

    private static class BodyInfo{
        public final Body body;
        public final String type;
        public final GameObject gameObject;
        public final int id;
        private BodyInfo(Body body, String type, GameObject gameObject, int id) {
            this.body = body;
            this.type = type;
            this.gameObject = gameObject;
            this.id = id;
        }
        public AddGameObjectMessage createAddMessage(){
            return new AddGameObjectMessage(this.id, this.type, this.body.getPosition().cpy(), this.body.getAngle());
        }
        public MoveGameObjectMessage createMoveMessage(){
            return new MoveGameObjectMessage(this.id, this.body.getPosition().cpy(), this.body.getAngle());
        }
    }
}