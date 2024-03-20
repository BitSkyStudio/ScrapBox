package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
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
    private MouseJoint pinching;
    public Player(Server server, IServerConnection connection) {
        this.server = server;
        this.connection = connection;
        this.pinching = null;
    }
    public void tick(){
        for(MessageC2S message : this.connection.read()){
            if(message instanceof ToggleGamePaused){
                server.paused = !server.paused;
            }
            if(message instanceof GameObjectPinch){
                if(pinching != null){
                    server.physics.destroyJoint(pinching);
                }
                GameObjectPinch gameObjectPinch = (GameObjectPinch) message;
                GameObject gameObject = server.gameObjects.get(gameObjectPinch.id);
                gameObject.setRotatable(false);
                MouseJointDef mouseJointDef = new MouseJointDef();
                mouseJointDef.bodyA = server.terrain.body;
                mouseJointDef.bodyB = gameObject.body;
                mouseJointDef.target.set(gameObjectPinch.offset.add(gameObject.body.getPosition()));
                mouseJointDef.maxForce = 10000;
                mouseJointDef.collideConnected = true;
                pinching = (MouseJoint) server.physics.createJoint(mouseJointDef);
            }
            if(message instanceof GameObjectRelease){
                if(pinching != null){
                    GameObject gameObject = this.getPinching();
                    gameObject.setGhost(false);
                    gameObject.setRotatable(true);
                    server.physics.destroyJoint(pinching);
                    pinching = null;
                    this.send(new ShowActivePossibleWelds(new ArrayList<>()));
                }
            }
            if(message instanceof MouseMoved){
                MouseMoved mouseMoved = (MouseMoved) message;
                if(pinching != null){
                    pinching.setTarget(mouseMoved.position);
                    GameObject gameObject = (GameObject) pinching.getBodyB().getUserData();
                    ArrayList<ShowActivePossibleWelds.PossibleWeld> welds = new ArrayList<>();
                    for(GameObject.WeldCandidate weld : gameObject.getPossibleWelds()){
                        welds.add(new ShowActivePossibleWelds.PossibleWeld(weld.first.getPosition(), weld.second.getPosition()));
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
                        joint.referenceAngle = 0;
                        joint.lowerAngle = -0.01f;
                        joint.upperAngle = 0.01f;
                        this.server.physics.createJoint(joint);
                    }
                }
            }
        }
    }
    private GameObject getPinching(){
        if(pinching == null){
            return null;
        }
        return (GameObject) pinching.getBodyB().getUserData();
    }
    public void send(MessageS2C message){
        this.connection.send(message);
    }
    public void sendAll(ArrayList<MessageS2C> messages){
        for(MessageS2C message : messages){
            this.connection.send(message);
        }
    }
}
