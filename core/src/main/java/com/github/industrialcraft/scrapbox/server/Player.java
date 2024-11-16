package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.github.industrialcraft.scrapbox.common.EObjectInteractionMode;
import com.github.industrialcraft.scrapbox.common.net.IConnection;
import com.github.industrialcraft.scrapbox.common.net.msg.*;
import com.github.industrialcraft.scrapbox.server.game.ControllerGameObject;
import com.github.industrialcraft.scrapbox.server.game.RopeGameObject;
import com.github.industrialcraft.scrapbox.server.game.StickGameObject;

import java.util.*;

public class Player extends GameObject{
    public final Server server;
    public final IConnection connection;
    private PinchingData pinching;
    private boolean isDisconnected;
    public final UUID uuid;
    public ArrayList<Rectangle> buildableAreas;
    public EnumMap<EItemType, Float> inventory;
    public boolean infiniteItems;
    public Player(Server server, IConnection connection, GameObjectConfig config) {
        super(Vector2.Zero.cpy(), 0, server, config);
        this.server = server;
        this.connection = connection;
        this.pinching = null;
        this.isDisconnected = false;
        this.uuid = UUID.randomUUID();
        this.buildableAreas = new ArrayList<>();

        connection.send(new GamePausedState(server.paused));

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        setBody("base", "player", server.physics.createBody(bodyDef));

        //setBuildableAreas(new ArrayList<>(Collections.singleton(new Rectangle(0, 0, 5, 5))));

        this.inventory = new EnumMap<>(EItemType.class);
        this.infiniteItems = true;
        syncInventory();
    }
    public void setInfiniteItems(boolean infiniteItems){
        this.infiniteItems = infiniteItems;
        syncInventory();
    }
    public float getItemCount(EItemType itemType){
        if(infiniteItems)
            return Float.POSITIVE_INFINITY;
        return this.inventory.getOrDefault(itemType, 0f);
    }
    public void removeItems(EItemType itemType, int count){
        if(infiniteItems)
            return;
        this.inventory.put(itemType, this.inventory.get(itemType)-count);
        syncInventory();
    }
    public void addItems(EItemType itemType, int count){
        if(infiniteItems)
            return;
        this.inventory.put(itemType, this.inventory.get(itemType)+count);
        syncInventory();
    }
    private void syncInventory(){
        connection.send(new UpdateInventory(this.inventory.clone()));
    }
    public void setBuildableAreas(ArrayList<Rectangle> buildableAreas){
        this.buildableAreas = buildableAreas;
        connection.send(new UpdateBuildableAreas(buildableAreas));
    }
    public boolean isInBuildableArea(float x, float y){
        if(buildableAreas.isEmpty())
            return true;
        for(Rectangle area : buildableAreas){
            if(area.contains(x, y))
                return true;
        }
        return false;
    }
    public boolean isInBuildableArea(){
        return isInBuildableArea(getBaseBody().getPosition().x, getBaseBody().getPosition().y);
    }
    @Override
    public void getAnimationData(ClientWorldManager.AnimationData animationData) {
        animationData.addString("color", Integer.toHexString(((int) this.uuid.getLeastSignificantBits()) >>> 8));
    }
    @Override
    public void damage(float amount, EDamageType damageType) {}
    public void tick(){
        if(isDisconnected){
            remove();
        }
        if(this.pinching != null){
            if(this.pinching.mouseJoint.getBodyB() == null){
                this.pinching = null;
            }
        }
        for(Object message : this.connection.read()){
            if(message instanceof ToggleGamePaused){
                ToggleGamePaused toggleGamePaused = (ToggleGamePaused) message;
                if(toggleGamePaused.step){
                    server.singleStep = true;
                } else {
                    server.paused = !server.paused;
                    GamePausedState pausedStateMessage = new GamePausedState(server.paused);
                    for(Player client : server.players){
                        client.send(pausedStateMessage);
                    }
                }
            }
            if(message instanceof GameObjectPinch){
                if(pinching != null){
                    server.physics.destroyJoint(pinching.mouseJoint);
                }
                GameObjectPinch gameObjectPinch = (GameObjectPinch) message;
                GameObject gameObject = server.gameObjects.get(gameObjectPinch.id);
                if(gameObject == null){
                    continue;
                }
                if(!isInBuildableArea() && gameObject.getLocalMode() != EObjectInteractionMode.Ghost)
                    continue;
                if(gameObject.vehicle.getMode() == EObjectInteractionMode.Static){
                    gameObject.vehicle.setMode(EObjectInteractionMode.Normal);
                }
                MouseJointDef mouseJointDef = new MouseJointDef();
                mouseJointDef.bodyA = server.terrain.body;
                mouseJointDef.bodyB = gameObject.getBaseBody();
                mouseJointDef.target.set(gameObject.vehicle.getCenterOfMass());
                mouseJointDef.maxForce = 10000;
                mouseJointDef.collideConnected = true;
                Vector2 offset = gameObject.vehicle.getCenterOfMass().sub(gameObject.getBaseBody().getWorldCenter().cpy().add(gameObjectPinch.offset));
                pinching = new PinchingData((MouseJoint) server.physics.createJoint(mouseJointDef), offset);
            }
            if(message instanceof GameObjectRelease){
                if(!isInBuildableArea() && pinching != null && getPinching().getLocalMode() == EObjectInteractionMode.Ghost){
                    for(GameObject go : getPinching().vehicle.gameObjects.toArray(GameObject[]::new)){
                        go.remove();
                    }
                }
                clearPinched();
            }
            if(message instanceof MouseMoved){
                MouseMoved mouseMoved = (MouseMoved) message;
                getBaseBody().setTransform(mouseMoved.position.cpy(), 0);
                if(pinching != null) {
                    if(isInBuildableArea()) {
                        pinching.mouseJoint.setTarget(mouseMoved.position.add(pinching.offset));
                        GameObject gameObject = getPinching();
                        if (gameObject != null) {
                            if (gameObject.isSideUsed("center") && gameObject.getConnectionEdges().size() == 1) {
                                gameObject = gameObject.connections.get("center").other;
                            }
                            ArrayList<ShowActivePossibleWelds.PossibleWeld> welds = new ArrayList<>();
                            for (GameObject.WeldCandidate weld : gameObject.getPossibleWelds()) {
                                welds.add(new ShowActivePossibleWelds.PossibleWeld(weld.first.getPosition().cpy(), weld.second.getPosition().cpy()));
                            }
                            this.send(new ShowActivePossibleWelds(welds));
                        }
                    }
                }
            }
            if(message instanceof TrashObject){
                TrashObject trashObject = (TrashObject) message;
                for(GameObject go : server.gameObjects.get(trashObject.id).vehicle.gameObjects.toArray(GameObject[]::new)){
                    go.remove();
                }
            }
            if(message instanceof TakeObject){
                TakeObject takeObject = (TakeObject) message;
                GameObject gameObject = server.spawnGameObject(takeObject.position, 0, takeObject.type, null, takeObject.config);
                gameObject.vehicle.setMode(EObjectInteractionMode.Ghost);
                connection.send(new TakeObjectResponse(gameObject.getId(), takeObject.offset));
            }
            if(message instanceof PlaceTerrain){
                PlaceTerrain placeTerrain = (PlaceTerrain) message;
                server.terrain.placeFromMessage(placeTerrain);
            }
            if(message instanceof PinchingGhostToggle){
                if(!isInBuildableArea())
                    continue;
                GameObject pinching = getPinching();
                if(pinching != null){
                    if(pinching.vehicle.getMode() == EObjectInteractionMode.Normal){
                        pinching.vehicle.setMode(EObjectInteractionMode.Ghost);
                    } else {
                        pinching.vehicle.setMode(EObjectInteractionMode.Normal);
                    }
                }
            }
            if(message instanceof CommitWeld){
                GameObject pinching = getPinching();
                if(pinching != null){
                    if(pinching.isSideUsed("center") && pinching.getConnectionEdges().size() == 1){
                        pinching = pinching.connections.get("center").other;
                    }
                    for(GameObject.WeldCandidate weldCandidate : pinching.getPossibleWelds()){
                        GameObject.GameObjectConnectionEdge go1 = weldCandidate.second;
                        GameObject.GameObjectConnectionEdge go2 = weldCandidate.first;
                        server.joinGameObject(go1.gameObject, go1.name, go2.gameObject, go2.name);
                    }
                }
            }
            if(message instanceof LockGameObject){
                if(!isInBuildableArea())
                    continue;
                GameObject pinching = getPinching();
                if(pinching != null){
                    pinching.vehicle.setMode(EObjectInteractionMode.Static);
                    clearPinched();
                }
            }
            if(message instanceof PinchingRotate){
                PinchingRotate pinchingRotate = (PinchingRotate) message;
                GameObject pinching = getPinching();
                if(pinching != null){
                    if(pinching instanceof IPairObject){
                        ((IPairObject)pinching).changeDistance(pinchingRotate.rotation);
                    } else {
                        pinching.getBaseBody().applyAngularImpulse(-pinchingRotate.rotation * pinching.vehicle.getMass(), true);
                    }
                }
            }
            if(message instanceof OpenGameObjectEditUI){
                OpenGameObjectEditUI openGameObjectEditUI = (OpenGameObjectEditUI) message;
                GameObject go = server.gameObjects.get(openGameObjectEditUI.id);
                go.uiViewers.add(this);
                go.updateUI();
            }
            if(message instanceof CloseGameObjectEditUI){
                CloseGameObjectEditUI closeGameObjectEditUI = (CloseGameObjectEditUI) message;
                GameObject go = server.gameObjects.get(closeGameObjectEditUI.id);
                go.uiViewers.remove(this);
            }
            if(message instanceof CreateValueConnection){
                CreateValueConnection createValueConnection = (CreateValueConnection) message;
                GameObject input = server.gameObjects.get(createValueConnection.inputObjectId);
                GameObject output = server.gameObjects.get(createValueConnection.outputObjectId);
                input.createValueConnection(createValueConnection.inputId, new GameObject.ValueConnection(output, createValueConnection.outputId));
                input.updateUI();
            }
            if(message instanceof DestroyValueConnection){
                DestroyValueConnection destroyValueConnection = (DestroyValueConnection) message;
                GameObject input = server.gameObjects.get(destroyValueConnection.inputObjectId);
                input.destroyValueConnection(destroyValueConnection.inputId);
                input.updateUI();
            }
            if(message instanceof ControllerInput){
                ControllerInput controllerInput = (ControllerInput) message;
                GameObject gameObject = server.gameObjects.get(controllerInput.gameObjectId);
                if(gameObject instanceof ControllerGameObject){
                    ControllerGameObject controller = (ControllerGameObject) gameObject;
                    controller.input(controllerInput.key, controllerInput.down);
                }
            }
            if(message instanceof EditorUIInput){
                EditorUIInput editorUIInput = (EditorUIInput) message;
                GameObject gameObject = server.gameObjects.get(editorUIInput.gameObjectId);
                if(gameObject != null){
                    try {
                        gameObject.handleEditorUIInput(editorUIInput.elementId, editorUIInput.value);
                    } catch (Exception e){}
                    gameObject.updateUI();
                }
            }
            if(message instanceof DestroyJoint){
                DestroyJoint destroyJoint = (DestroyJoint) message;
                GameObject gameObject = server.gameObjects.get(destroyJoint.gameObjectId);
                gameObject.disconnect(destroyJoint.name);
            }
            if(message instanceof RequestControllerState){
                RequestControllerState requestControllerState = (RequestControllerState) message;
                GameObject gameObject = server.gameObjects.get(requestControllerState.gameObjectId);
                if(gameObject instanceof ControllerGameObject){
                    boolean[] state = new boolean[10];
                    System.arraycopy(((ControllerGameObject) gameObject).inputs, 0, state, 0, 10);
                    this.send(new ResponseControllerState(state));
                }
            }
            if(message instanceof ChangeObjectHealth){
                ChangeObjectHealth changeObjectHealthMessage = (ChangeObjectHealth) message;
                GameObject gameObject = server.gameObjects.get(changeObjectHealthMessage.gameObjectId);
                if(gameObject != null){
                    gameObject.damage(changeObjectHealthMessage.health, EDamageType.Wrench);
                }
            }
            if(message instanceof CreateGearConnection){
                CreateGearConnection createGearConnectionMessage = (CreateGearConnection) message;
                GameObject goA = server.gameObjects.get(createGearConnectionMessage.objectA);
                GameObject goB = server.gameObjects.get(createGearConnectionMessage.objectB);
                if(goA != null && goB != null){
                    goA.connectGearJoint(goB, createGearConnectionMessage.gearRatioA, createGearConnectionMessage.gearRatioB);
                }
            }
            if(message instanceof DestroyGearConnection){
                DestroyGearConnection destroyGearConnectionMessage = (DestroyGearConnection) message;
                GameObject goA = server.gameObjects.get(destroyGearConnectionMessage.objectA);
                GameObject goB = server.gameObjects.get(destroyGearConnectionMessage.objectB);
                if(goA != null && goB != null){
                    goA.disconnectGearJoint(goB);
                }
            }
        }
    }
    @Override
    public HashMap<String, ConnectionEdge> getConnectionEdges() {
        return new HashMap<>();
    }
    public void disconnect(){
        this.isDisconnected = true;
        clearPinched();
    }
    public boolean isDisconnected() {
        return isDisconnected;
    }
    public void clearPinched(){
        if(pinching != null){
            GameObject gameObject = this.getPinching();
            if(gameObject.vehicle.getMode() == EObjectInteractionMode.Ghost){
                gameObject.vehicle.setMode(EObjectInteractionMode.Normal);
            }
            server.physics.destroyJoint(pinching.mouseJoint);
            pinching = null;
            this.send(new ShowActivePossibleWelds(new ArrayList<>()));
        }
    }
    public GameObject getPinching(){
        if(pinching == null){
            return null;
        }
        Body body = pinching.mouseJoint.getBodyB();
        if(body == null){
            return null;
        }
        return (GameObject) body.getUserData();
    }
    public void send(Object message){
        this.connection.send(message);
    }
    public void sendAll(ArrayList<Object> messages){
        for(Object message : messages){
            this.connection.send(message);
        }
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(uuid, player.uuid);
    }
    @Override
    public int hashCode() {
        return Objects.hash(uuid);
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
