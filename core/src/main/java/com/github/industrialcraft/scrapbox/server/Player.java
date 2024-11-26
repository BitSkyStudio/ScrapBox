package com.github.industrialcraft.scrapbox.server;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.github.industrialcraft.scrapbox.common.EObjectInteractionMode;
import com.github.industrialcraft.scrapbox.common.net.IConnection;
import com.github.industrialcraft.scrapbox.common.net.msg.*;
import com.github.industrialcraft.scrapbox.server.game.ControllerGameObject;

import java.util.*;
import java.util.stream.Collectors;

public class Player extends GameObject{
    public final Server server;
    public final IConnection connection;
    private PinchingData pinching;
    private boolean isDisconnected;
    public final UUID uuid;
    public PlayerTeam team;
    public Player(Server server, IConnection connection, GameObjectConfig config) {
        super(Vector2.Zero.cpy(), 0, server, config);
        this.server = server;
        this.connection = connection;
        this.pinching = null;
        this.isDisconnected = false;
        this.uuid = UUID.randomUUID();

        connection.send(new GamePausedState(server.paused));
        connection.send(new PlayerTeamList((ArrayList<String>) server.teams.stream().map(playerTeam -> playerTeam.name).collect(Collectors.toCollection(ArrayList::new))));

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        setBody("base", "player", server.physics.createBody(bodyDef));

        this.team = null;
    }
    public void setTeam(PlayerTeam team){
        if(this.team != null){
            this.team.players.remove(this);
        }
        this.team = team;
        team.players.add(this);
        team.syncPlayer(this);
    }
    public boolean isInBuildableArea(){
        if(this.team == null)
            return false;
        return team.isInBuildableArea(getBaseBody().getPosition().x, getBaseBody().getPosition().y);
    }
    @Override
    public void getAnimationData(ClientWorldManager.AnimationData animationData) {
        animationData.addString("color", Integer.toHexString(((int) this.uuid.getLeastSignificantBits()) >>> 8));
    }
    public boolean isGameObjectInBuildableArea(GameObject go){
        if(this.team == null)
            return false;
        if(go == null)
            return false;
        Vector2 position = go.getBaseBody().getPosition();
        return team.isInBuildableArea(position.x, position.y);
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
                if(team == null)
                    continue;
                if(getPinching() != null && getPinching().isRemoved()){
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
                pinching = new PinchingData((MouseJoint) server.physics.createJoint(mouseJointDef), offset, gameObject);
            }
            if(message instanceof GameObjectRelease){
                if(!isInBuildableArea() && getPinching() != null && getPinching().getLocalMode() == EObjectInteractionMode.Ghost){
                    trashVehicle(getPinching());
                }
                clearPinched();
            }
            if(message instanceof MouseMoved){
                MouseMoved mouseMoved = (MouseMoved) message;
                getBaseBody().setTransform(mouseMoved.position.cpy(), 0);
                if(pinching != null) {
                    if(isInBuildableArea()) {
                        GameObject gameObject = getPinching();
                        if (gameObject != null && !gameObject.isRemoved()) {
                            pinching.mouseJoint.setTarget(mouseMoved.position.add(pinching.offset));
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
                if(team == null)
                    continue;
                TrashObject trashObject = (TrashObject) message;
                GameObject go = server.gameObjects.get(trashObject.id);
                if(go != null)
                    trashVehicle(go);
            }
            if(message instanceof TakeObject){
                if(team == null)
                    continue;
                TakeObject takeObject = (TakeObject) message;
                EnumMap<EItemType, Float> cost = server.getGameObjectCost(takeObject.type, takeObject.config);
                boolean failed = false;
                for(Map.Entry<EItemType, Float> entry : cost.entrySet()){
                    if(team.getItemCount(entry.getKey()) < entry.getValue())
                        failed = true;
                }
                if(!failed) {
                    for(Map.Entry<EItemType, Float> entry : cost.entrySet()){
                        team.removeItems(entry.getKey(), entry.getValue());
                    }
                    GameObject gameObject = server.spawnGameObject(takeObject.position, 0, takeObject.type, null, takeObject.config);
                    gameObject.vehicle.setMode(EObjectInteractionMode.Ghost);
                    connection.send(new TakeObjectResponse(gameObject.getId(), takeObject.offset));
                }
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
                if(!isGameObjectInBuildableArea(go))
                    continue;
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
                if(!isGameObjectInBuildableArea(input) || !isGameObjectInBuildableArea(output))
                    continue;
                input.createValueConnection(createValueConnection.inputId, new GameObject.ValueConnection(output, createValueConnection.outputId));
                input.updateUI();
            }
            if(message instanceof DestroyValueConnection){
                DestroyValueConnection destroyValueConnection = (DestroyValueConnection) message;
                GameObject input = server.gameObjects.get(destroyValueConnection.inputObjectId);
                if(!isGameObjectInBuildableArea(input))
                    continue;
                input.destroyValueConnection(destroyValueConnection.inputId);
                input.updateUI();
            }
            if(message instanceof ControllerInput){
                ControllerInput controllerInput = (ControllerInput) message;
                GameObject gameObject = server.gameObjects.get(controllerInput.gameObjectId);
                if(gameObject instanceof ControllerGameObject){
                    ControllerGameObject controller = (ControllerGameObject) gameObject;
                    if(this.team != null && (controller.authorizedTeam == null || controller.authorizedTeam.equals(this.team.name)))
                        controller.input(controllerInput.key, controllerInput.down);
                }
            }
            if(message instanceof EditorUIInput){
                EditorUIInput editorUIInput = (EditorUIInput) message;
                GameObject gameObject = server.gameObjects.get(editorUIInput.gameObjectId);
                if(!isGameObjectInBuildableArea(gameObject))
                    continue;
                if(gameObject != null){
                    try {
                        gameObject.handleEditorUIInput(editorUIInput.elementId, editorUIInput.value, this);
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
                if(!isGameObjectInBuildableArea(gameObject))
                    continue;
                if(gameObject != null){
                    gameObject.damage(changeObjectHealthMessage.health, EDamageType.Wrench);
                }
            }
            if(message instanceof CreateGearConnection){
                CreateGearConnection createGearConnectionMessage = (CreateGearConnection) message;
                GameObject goA = server.gameObjects.get(createGearConnectionMessage.objectA);
                GameObject goB = server.gameObjects.get(createGearConnectionMessage.objectB);
                if(!isGameObjectInBuildableArea(goA) || !isGameObjectInBuildableArea(goB))
                    continue;
                if(goA != null && goB != null){
                    goA.connectGearJoint(goB, createGearConnectionMessage.gearRatioA, createGearConnectionMessage.gearRatioB);
                }
            }
            if(message instanceof DestroyGearConnection){
                DestroyGearConnection destroyGearConnectionMessage = (DestroyGearConnection) message;
                GameObject goA = server.gameObjects.get(destroyGearConnectionMessage.objectA);
                GameObject goB = server.gameObjects.get(destroyGearConnectionMessage.objectB);
                if(!isGameObjectInBuildableArea(goA) || !isGameObjectInBuildableArea(goB))
                    continue;
                if(goA != null && goB != null){
                    goA.disconnectGearJoint(goB);
                }
            }
            if(message instanceof PlayerTeamUpdate){
                PlayerTeamUpdate playerTeamUpdateMessage = (PlayerTeamUpdate) message;
                PlayerTeam team = server.getTeamByName(playerTeamUpdateMessage.team);
                if(team != null)
                    this.setTeam(team);
            }
        }
    }
    public void trashVehicle(GameObject gameObject){
        if(team == null)
            return;
        for(GameObject go : gameObject.vehicle.gameObjects.toArray(GameObject[]::new)){
            go.remove();
            for(Map.Entry<EItemType, Float> entry : server.getGameObjectCost(server.getGameObjectId(go), go.config).entrySet()){
                team.addItems(entry.getKey(), entry.getValue());
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
            if(gameObject == null)
                return;
            if(gameObject.vehicle.getMode() == EObjectInteractionMode.Ghost){
                gameObject.vehicle.setMode(EObjectInteractionMode.Normal);
            }
            if(!gameObject.isRemoved()) {
                server.physics.destroyJoint(pinching.mouseJoint);
            }
            pinching = null;
            this.send(new ShowActivePossibleWelds(new ArrayList<>()));
        }
    }
    public GameObject getPinching(){
        if(pinching == null){
            return null;
        }
        if(pinching.gameObject.isRemoved())
            return null;
        return pinching.gameObject;
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
        public final GameObject gameObject;
        public PinchingData(MouseJoint mouseJoint, Vector2 offset, GameObject gameObject) {
            this.mouseJoint = mouseJoint;
            this.offset = offset;
            this.gameObject = gameObject;
        }
    }
}
