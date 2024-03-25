package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.github.industrialcraft.scrapbox.common.net.IServerConnection;
import com.github.industrialcraft.scrapbox.common.net.MessageC2S;
import com.github.industrialcraft.scrapbox.common.net.MessageS2C;
import com.github.industrialcraft.scrapbox.common.net.msg.*;

import java.util.ArrayList;

public class Player {
    public final Server server;
    public final IServerConnection connection;
    private PinchingData pinching;
    public Player(Server server, IServerConnection connection) {
        this.server = server;
        this.connection = connection;
        this.pinching = null;
    }
    public void tick(){
        if(this.pinching != null){
            if(this.pinching.mouseJoint.getBodyB() == null){
                this.pinching = null;
            }
        }
        for(MessageC2S message : this.connection.read()){
            if(message instanceof ToggleGamePaused){
                server.paused = !server.paused;
            }
            if(message instanceof GameObjectPinch){
                if(pinching != null){
                    server.physics.destroyJoint(pinching.mouseJoint);
                }
                GameObjectPinch gameObjectPinch = (GameObjectPinch) message;
                GameObject gameObject = server.gameObjects.get(gameObjectPinch.id);
                gameObject.setLocked(false);
                MouseJointDef mouseJointDef = new MouseJointDef();
                mouseJointDef.bodyA = server.terrain.body;
                mouseJointDef.bodyB = gameObject.body;
                mouseJointDef.target.set(gameObject.vehicle.getCenterOfMass());
                mouseJointDef.maxForce = 10000;
                mouseJointDef.collideConnected = true;
                pinching = new PinchingData((MouseJoint) server.physics.createJoint(mouseJointDef), gameObjectPinch.offset);
            }
            if(message instanceof GameObjectRelease){
                clearPinched();
            }
            if(message instanceof MouseMoved){
                MouseMoved mouseMoved = (MouseMoved) message;
                if(pinching != null){
                    pinching.mouseJoint.setTarget(mouseMoved.position.cpy().sub(pinching.offset));
                    GameObject gameObject = (GameObject) pinching.mouseJoint.getBodyB().getUserData();
                    ArrayList<ShowActivePossibleWelds.PossibleWeld> welds = new ArrayList<>();
                    for(GameObject.WeldCandidate weld : gameObject.getPossibleWelds()){
                        welds.add(new ShowActivePossibleWelds.PossibleWeld(weld.first.getPosition().cpy(), weld.second.getPosition().cpy()));
                    }
                    this.send(new ShowActivePossibleWelds(welds));
                }
            }
            if(message instanceof TrashObject){
                TrashObject trashObject = (TrashObject) message;
                server.gameObjects.get(trashObject.id).remove();
            }
            if(message instanceof TakeObject){
                TakeObject takeObject = (TakeObject) message;
                GameObject gameObject = server.spawnGameObject(takeObject.position, takeObject.type);
                connection.send(new TakeObjectResponse(gameObject.id, takeObject.offset));
            }
            if(message instanceof PlaceTerrain){
                PlaceTerrain placeTerrain = (PlaceTerrain) message;
                server.terrain.place(placeTerrain);
            }
            if(message instanceof PinchingSetGhost){
                PinchingSetGhost pinchingSetGhost = (PinchingSetGhost) message;
                GameObject pinching = getPinching();
                if(pinching != null){
                    pinching.setGhost(pinchingSetGhost.isGhost);
                }
            }
            if(message instanceof CommitWeld){
                GameObject pinching = getPinching();
                if(pinching != null){
                    for(GameObject.WeldCandidate weldCandidate : pinching.getPossibleWelds()){
                        RevoluteJointDef joint = new RevoluteJointDef();
                        joint.bodyA = weldCandidate.first.gameObject.body;
                        joint.bodyB = weldCandidate.second.gameObject.body;
                        joint.localAnchorA.set(weldCandidate.first.connectionEdge.offset);
                        joint.localAnchorB.set(weldCandidate.second.connectionEdge.offset);
                        joint.enableLimit = true;
                        //System.out.println(weldCandidate.angle);
                        //joint.referenceAngle = (float) -weldCandidate.angle;
                        //joint.referenceAngle = (float) Math.PI;
                        joint.referenceAngle = weldCandidate.second.gameObject.body.getAngle() - weldCandidate.first.gameObject.body.getAngle();
                        joint.lowerAngle = 0f;
                        joint.upperAngle = 0f;
                        this.server.physics.createJoint(joint);
                        weldCandidate.first.gameObject.vehicle.add(weldCandidate.second.gameObject);
                    }
                }
            }
            if(message instanceof LockGameObject){
                GameObject pinching = getPinching();
                if(pinching != null){
                    pinching.setLocked(true);
                    clearPinched();
                }
            }
        }
    }
    private void clearPinched(){
        if(pinching != null){
            GameObject gameObject = this.getPinching();
            gameObject.setGhost(false);
            server.physics.destroyJoint(pinching.mouseJoint);
            pinching = null;
            this.send(new ShowActivePossibleWelds(new ArrayList<>()));
        }
    }
    private GameObject getPinching(){
        if(pinching == null){
            return null;
        }
        return (GameObject) pinching.mouseJoint.getBodyB().getUserData();
    }
    public void send(MessageS2C message){
        this.connection.send(message);
    }
    public void sendAll(ArrayList<MessageS2C> messages){
        for(MessageS2C message : messages){
            this.connection.send(message);
        }
    }
    public static class PinchingData{
        public final MouseJoint mouseJoint;
        public final Vector2 offset;
        public PinchingData(MouseJoint mouseJoint, Vector2 offset) {
            this.mouseJoint = mouseJoint;
            this.offset = offset;
        }
    }
}
