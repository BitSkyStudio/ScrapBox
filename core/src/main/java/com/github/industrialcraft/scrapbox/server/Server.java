package com.github.industrialcraft.scrapbox.server;

import clipper2.core.PathD;
import clipper2.core.PathsD;
import clipper2.core.PointD;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.github.industrialcraft.netx.LANBroadcaster;
import com.github.industrialcraft.netx.NetXServer;
import com.github.industrialcraft.netx.ServerMessage;
import com.github.industrialcraft.netx.SocketUser;
import com.github.industrialcraft.scrapbox.common.net.MessageRegistryCreator;
import com.github.industrialcraft.scrapbox.common.net.msg.DisconnectMessage;
import com.github.industrialcraft.scrapbox.common.net.msg.SetGameState;
import com.github.industrialcraft.scrapbox.common.net.msg.SubmitPassword;
import com.github.industrialcraft.scrapbox.server.game.*;
import com.github.industrialcraft.scrapbox.common.net.LocalConnection;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.github.industrialcraft.scrapbox.server.GameObject.HALF_PI;

public class Server {
    public static Vector2 GRAVITY = new Vector2(0, -9.81f * 1.2f);

    public final ArrayList<Player> players;
    public final HashMap<Integer,GameObject> gameObjects;
    private final ArrayList<GameObject> newGameObjects;
    public final World physics;
    public final Terrain terrain;
    public final ClientWorldManager clientWorldManager;
    public final NetXServer networkServer;
    private boolean stopped;
    public boolean paused;
    public boolean singleStep;
    private int tickCount;
    private int runningTickCount;
    private final UUID uuid;
    public final File saveFile;
    public final ArrayList<Vector3> scheduledExplosions;
    public String password;
    public Server(int port, File saveFile) {
        this.password = null;
        this.saveFile = saveFile;
        this.uuid = UUID.randomUUID();
        this.players = new ArrayList<>();
        this.physics = new World(GRAVITY, true);
        this.terrain = new Terrain(this);
        this.terrain.registerTerrainType("dirt", new Terrain.TerrainType(2, 0.05f));
        this.terrain.registerTerrainType("stone", new Terrain.TerrainType(1, 0.3f));
        this.terrain.registerTerrainType("ice", new Terrain.TerrainType(0.f, 0.1f));
        this.gameObjects = new HashMap<>();
        this.newGameObjects = new ArrayList<>();
        this.clientWorldManager = new ClientWorldManager(this);
        this.networkServer = new NetXServer(port, MessageRegistryCreator.create());
        this.networkServer.start();
        this.stopped = false;
        this.tickCount = 0;
        this.runningTickCount = 0;
        this.scheduledExplosions = new ArrayList<>();
        this.paused = false;
        this.singleStep = false;
        this.physics.setContactFilter((fixtureA, fixtureB) -> {
            Filter filterA = fixtureA.getFilterData();
            Filter filterB = fixtureB.getFilterData();
            boolean collide =
                (filterA.maskBits & filterB.categoryBits) != 0 &&
                    (filterA.categoryBits & filterB.maskBits) != 0;
            if(!collide){
                return false;
            }
            Body bodyA = fixtureA.getBody();
            Body bodyB = fixtureB.getBody();
            Object userDataA = bodyA.getUserData();
            Object userDataB = bodyB.getUserData();
            if(userDataA instanceof GameObject){
                if(!((GameObject) userDataA).collidesWith(fixtureA, fixtureB)){
                    return false;
                }
            }
            if(userDataB instanceof GameObject){
                if(!((GameObject) userDataB).collidesWith(fixtureB, fixtureA)){
                    return false;
                }
            }
            return true;
        });
        this.physics.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Fixture fixtureA = contact.getFixtureA();
                Fixture fixtureB = contact.getFixtureB();
                Body bodyA = fixtureA.getBody();
                Body bodyB = fixtureB.getBody();
                Object userDataA = bodyA.getUserData();
                Object userDataB = bodyB.getUserData();
                if(userDataA instanceof GameObject){
                    ((GameObject) userDataA).onCollision(fixtureA, fixtureB);
                }
                if(userDataB instanceof GameObject){
                    ((GameObject) userDataB).onCollision(fixtureB, fixtureA);
                }
            }
            @Override
            public void endContact(Contact contact) {}
            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {}
            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
    }

    public int getTicks() {
        return runningTickCount;
    }

    public LocalConnection joinLocalPlayer(){
        ConcurrentLinkedQueue<Object> write = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Object> read = new ConcurrentLinkedQueue<>();
        this.addPlayer(new Player(this, new LocalConnection(write, read), GameObject.GameObjectConfig.DEFAULT));
        return new LocalConnection(read, write);
    }
    public  <T extends GameObject> T spawnGameObject(Vector2 position, float rotation, GameObject.GameObjectSpawner<T> spawner, UUID uuid, GameObject.GameObjectConfig config){
        T gameObject = spawner.spawn(position, rotation, this, config);
        if(uuid != null){
            gameObject.uuid = uuid;
        }
        this.newGameObjects.add(gameObject);
        return gameObject;
    }
    public GameObject spawnGameObject(Vector2 position, float rotation, String type, UUID uuid, GameObject.GameObjectConfig config){
        if(type.equals("frame")){
            return spawnGameObject(position, rotation, FrameGameObject::new, uuid, config);
        }
        if(type.equals("wheel")){
            return spawnGameObject(position, rotation, SimpleWheelGameObject::new, uuid, config);
        }
        if(type.equals("sticky_wheel")){
            return spawnGameObject(position, rotation, StickyWheelGameObject::new, uuid, config);
        }
        if(type.equals("cutting_wheel")){
            return spawnGameObject(position, rotation, CuttingWheelGameObject::new, uuid, config);
        }
        if(type.equals("balloon")){
            return spawnGameObject(position, rotation, BalloonGameObject::new, uuid, config);
        }
        if(type.equals("controller")){
            return spawnGameObject(position, rotation, ControllerGameObject::new, uuid, config);
        }
        if(type.equals("puncher")){
            return spawnGameObject(position, rotation, PunchBoxGameObject::new, uuid, config);
        }
        if(type.equals("propeller")){
            return spawnGameObject(position, rotation, PropellerGameObject::new, uuid, config);
        }
        if(type.equals("tnt")){
            return spawnGameObject(position, rotation, TntGameObject::new, uuid, config);
        }
        if(type.equals("rotator")){
            return spawnGameObject(position, rotation, RotatorGameObject::new, uuid, config);
        }
        if(type.equals("cannon")){
            return spawnGameObject(position, rotation, CannonGameObject::new, uuid, config);
        }
        if(type.equals("bullet")){
            return spawnGameObject(position, rotation, BulletGameObject::new, uuid, config);
        }
        if(type.equals("position_sensor")){
            return spawnGameObject(position, rotation, PositionSensorGameObject::new, uuid, config);
        }
        if(type.equals("distance_sensor")){
            return spawnGameObject(position, rotation, DistanceSensorGameObject::new, uuid, config);
        }
        if(type.equals("display")){
            return spawnGameObject(position, rotation, DisplayGameObject::new, uuid, config);
        }
        if(type.equals("math_unit")){
            return spawnGameObject(position, rotation, MathUnitGameObject::new, uuid, config);
        }
        if(type.equals("explosion_particle")){
            return spawnGameObject(position, rotation, ExplosionParticleGameObject::new, uuid, config);
        }
        if(type.equals("pid_controller")){
            return spawnGameObject(position, rotation, PIDControllerGameObject::new, uuid, config);
        }
        if(type.equals("weight")){
            return spawnGameObject(position, rotation, WeightGameObject::new, uuid, config);
        }
        if(type.equals("rope")){
            return spawnGameObject(position, rotation, RopeGameObject::new, uuid, config);
        }
        if(type.equals("grabber")){
            return spawnGameObject(position, rotation, GrabberGameObject::new, uuid, config);
        }
        if(type.equals("timer")){
            return spawnGameObject(position, rotation, TimerGameObject::new, uuid, config);
        }
        if(type.equals("stick")){
            return spawnGameObject(position, rotation, StickGameObject::new, uuid, config);
        }
        if(type.equals("piston")){
            return spawnGameObject(position, rotation, PistonGameObject::new, uuid, config);
        }
        throw new IllegalArgumentException("unknown type " + type);
    }
    private void addPlayer(Player player){
        this.players.add(player);
        this.newGameObjects.add(player);
        ArrayList<Object> messages = new ArrayList<>();
        this.clientWorldManager.addPlayer(player);
        player.send(this.terrain.createMessage());
        player.sendAll(messages);
    }
    private void tick(float deltaTime) {
        for(GameObject gameObject : this.newGameObjects){
            this.gameObjects.put(gameObject.getId(), gameObject);
        }
        this.newGameObjects.clear();
        this.gameObjects.entrySet().removeIf(entry -> {
            if(entry.getValue().isRemoved()){
                entry.getValue().destroy();
                return true;
            } else {
                return false;
            }
        });
        terrain.rebuildIfNeeded();
        boolean runTick = (!paused) || singleStep;
        for(GameObject gameObject : this.gameObjects.values()){
            if(runTick || gameObject instanceof Player)
                gameObject.tick();
        }
        if(runTick) {
            runningTickCount++;
            singleStep = false;
            int internalSteps = 20;
            for(int i = 0;i < internalSteps;i++) {
                for(GameObject gameObject : this.gameObjects.values()){
                    gameObject.internalTick();
                }
                this.physics.step(1.35f * deltaTime / internalSteps, 20, 20);
            }
        }
        for(Vector3 explosion : this.scheduledExplosions){
            Vector2 position = new Vector2(explosion.x, explosion.y);
            this.terrain.place("", position, explosion.z*2, false);
            Random random = new Random();
            for(int i = 0;i < 100;i++){
                ExplosionParticleGameObject go = spawnGameObject(position, 0f, ExplosionParticleGameObject::new, null, GameObject.GameObjectConfig.DEFAULT);
                go.power = explosion.z;
                go.getBaseBody().applyLinearImpulse(Vector2.Y.cpy().setAngleRad((float) (random.nextFloat()*Math.PI*2f)).scl(explosion.z*10*random.nextFloat()), go.getBaseBody().getWorldCenter(), true);
            }
        }
        this.scheduledExplosions.clear();
        this.clientWorldManager.updatePositions();
        this.players.removeIf(Player::isDisconnected);
        while(this.networkServer.visitMessage(new ServerMessage.Visitor() {
            @Override
            public void connect(SocketUser user) {
                if(password == null) {
                    user.send(new SetGameState(SetGameState.GameState.PLAY));
                    Player player = new Player(Server.this, new ServerNetXConnection(user), GameObject.GameObjectConfig.DEFAULT);
                    addPlayer(player);
                    user.setUserData(player);
                } else {
                    user.send(new SetGameState(SetGameState.GameState.REQUEST_PASSWORD));
                }
            }
            @Override
            public void disconnect(SocketUser user) {
                if(user.getUserData() != null)
                    ((Player)user.getUserData()).disconnect();
            }
            @Override
            public void message(SocketUser user, Object msg) {
                if(user.getUserData() == null){
                    if(msg instanceof SubmitPassword){
                        SubmitPassword submitPasswordMessage = (SubmitPassword) msg;
                        if(password == null || submitPasswordMessage.password.equals(password)){
                            user.send(new SetGameState(SetGameState.GameState.PLAY));
                            Player player = new Player(Server.this, new ServerNetXConnection(user), GameObject.GameObjectConfig.DEFAULT);
                            addPlayer(player);
                            user.setUserData(player);
                        } else {
                            user.send(new DisconnectMessage("wrong password"));
                            user.disconnect();
                        }
                    } else {
                        System.out.println("invalid handshake");
                        user.disconnect();
                    }
                } else {
                    ((ServerNetXConnection) ((Player) user.getUserData()).connection).queue.add(msg);
                }
            }
        }));
        if(tickCount%20==1){
            InetSocketAddress address = networkServer.getAddress();
            if(address != null) {
                JsonValue json = new JsonValue(JsonValue.ValueType.object);
                json.addChild("id", new JsonValue(uuid.toString()));
                json.addChild("port", new JsonValue(address.getPort()));
                try {
                    LANBroadcaster.broadcast(json.toJson(JsonWriter.OutputType.json), InetAddress.getByName("230.1.2.3"), 4321);
                } catch (Exception ignored){}
            }
        }
        int autoSaveAfterTicks = 20*60;
        if(tickCount%autoSaveAfterTicks==autoSaveAfterTicks-1){
            try {
                FileOutputStream stream = new FileOutputStream(saveFile);
                dumpToSaveFile().toStream(new DataOutputStream(stream));
                stream.close();
                System.out.println("autosaved");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public SaveFile dumpToSaveFile(){
        SaveFile saveFile = new SaveFile(new HashMap<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        this.terrain.terrain.forEach((s, pathDS) -> {
            ArrayList<ArrayList<Vector2>> paths = new ArrayList<>();
            for(PathD path : pathDS){
                ArrayList<Vector2> realPath = new ArrayList<>();
                for(PointD pointD : path){
                    realPath.add(new Vector2((float) pointD.x, (float) pointD.y));
                }
                paths.add(realPath);
            }
            saveFile.terrain.put(s, paths);
        });
        this.gameObjects.values().forEach(gameObject -> {
            if(gameObject.getType() != null && !gameObject.isRemoved()) {
                if (gameObject == gameObject.vehicle.gameObjects.get(0)) {
                    saveFile.savedVehicles.add(gameObject.vehicle.save());
                }
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try {
                    gameObject.save(new DataOutputStream(outputStream));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                saveFile.savedGameObjects.add(new SaveFile.SavedGameObject(gameObject.getType(), gameObject.uuid, gameObject.getBaseBody().getPosition().cpy(), gameObject.getBaseBody().getAngle(), outputStream.toByteArray(), gameObject.config));
                for (Map.Entry<String, GameObject.ConnectionData> entry : gameObject.connections.entrySet()) {
                    if (gameObject.getId() < entry.getValue().other.getId()) {
                        saveFile.savedJoints.add(new SaveFile.SavedJoint(gameObject.uuid, entry.getKey(), entry.getValue().other.uuid, entry.getValue().otherName));
                    }
                }
            }
        });
        return saveFile;
    }
    public void createExplosion(Vector2 position, float strength){
        this.scheduledExplosions.add(new Vector3(position.x, position.y, strength));
    }
    public GameObject getGameObjectByUUID(UUID uuid){
        return this.gameObjects.values().stream().filter(gameObject -> gameObject.uuid.equals(uuid)).findAny().or(() -> this.newGameObjects.stream().filter(gameObject -> gameObject.uuid.equals(uuid)).findAny()).orElse(null);
    }
    public void loadSaveFile(SaveFile saveFile){
        this.gameObjects.forEach((integer, gameObject) -> gameObject.remove());
        this.gameObjects.clear();
        this.players.forEach(Player::clearPinched);
        HashMap<UUID,byte[]> data = new HashMap<>();
        for(SaveFile.SavedGameObject gameObject : saveFile.savedGameObjects){
            try {
                spawnGameObject(gameObject.position, gameObject.rotation, gameObject.type, gameObject.id, gameObject.config);
                data.put(gameObject.id, gameObject.data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.terrain.terrain.clear();
        for(Map.Entry<String, ArrayList<ArrayList<Vector2>>> entry : saveFile.terrain.entrySet()){
            PathsD paths = new PathsD();
            for(ArrayList<Vector2> pathsReal : entry.getValue()){
                PathD path = new PathD();
                for(Vector2 point : pathsReal){
                    path.add(new PointD(point.x, point.y));
                }
                paths.add(path);
            }
            this.terrain.terrain.put(entry.getKey(), paths);
        }
        for(SaveFile.SavedJoint joint : saveFile.savedJoints){
            GameObject first = getGameObjectByUUID(joint.first);
            GameObject second = getGameObjectByUUID(joint.second);
            try {
                joinGameObject(first, joint.firstName, second, joint.secondName);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        saveFile.savedVehicles.forEach(vehicle -> {
            GameObject gameObject = getGameObjectByUUID(vehicle.firstGameObjectId);
            gameObject.vehicle.load(vehicle);
        });
        data.forEach((uuid1, bytes) -> {
            try {
                getGameObjectByUUID(uuid1).load(new DataInputStream(new ByteArrayInputStream(bytes)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    public void joinGameObject(GameObject first, String firstName, GameObject second, String secondName){
        /*if(second instanceof FrameGameObject){
            GameObject tmpGameObject = first;
            first = second;
            second = tmpGameObject;
            String tmpName = firstName;
            firstName = secondName;
            secondName = tmpName;
        }
        if(!(first instanceof FrameGameObject)){
            throw new RuntimeException("one of joined must be frame");
        }*/
        Body firstBase = first.getBaseBody();
        Body secondBase = second.getBaseBody();
        float rotationOffset = firstBase.getAngle()+((float) (Math.round((secondBase.getAngle()-firstBase.getAngle())/HALF_PI)*HALF_PI));
        secondBase.setTransform(secondBase.getPosition(), rotationOffset);
        secondBase.setTransform(secondBase.getPosition().add(first.getOpenConnections().get(firstName).getPosition().sub(second.getOpenConnections().get(secondName).getPosition())), secondBase.getAngle());
        //Joint joint = second.createJoint(secondName, first, firstName);
        Joint joint = createFixedJoint(first, firstName, second, secondName);
        first.connect(firstName, second, secondName, joint);
        second.connect(secondName, first, firstName, joint);
        first.vehicle.add(second);
    }
    private Joint createFixedJoint(GameObject first, String firstName, GameObject second, String secondName){
        RevoluteJointDef joint = new RevoluteJointDef();
        GameObject.ConnectionEdge firstEdge = first.getConnectionEdges().get(firstName);
        GameObject.ConnectionEdge secondEdge = second.getConnectionEdges().get(secondName);
        joint.bodyA = first.getBody(firstEdge.bodyName);
        joint.bodyB = second.getBody(secondEdge.bodyName);
        joint.localAnchorA.set(firstEdge.offset);
        joint.localAnchorB.set(secondEdge.offset);
        joint.enableLimit = true;
        //System.out.println(weldCandidate.angle);
        //joint.referenceAngle = (float) -weldCandidate.angle;
        //joint.referenceAngle = (float) Math.PI;
        joint.referenceAngle = (float) (Math.round((joint.bodyB.getAngle() - joint.bodyA.getAngle())/HALF_PI)*HALF_PI);
        joint.lowerAngle = 0f;
        joint.upperAngle = 0f;
        return physics.createJoint(joint);
    }
    public void start(){
        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            while(!stopped){
                synchronized (physics) {
                    try {
                        tick(1f/20);
                    } catch (Exception e) {
                        e.printStackTrace();
                        stop();
                    }
                }
                tickCount++;
                try {
                    int sleepTime = (int) (tickCount*50-(System.currentTimeMillis()-startTime));
                    if(sleepTime > 0)
                        Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            this.networkServer.close();
            this.physics.dispose();
            try {
                if(saveFile != null) {
                    FileOutputStream stream = new FileOutputStream(saveFile);
                    dumpToSaveFile().toStream(new DataOutputStream(stream));
                    stream.close();
                }
            } catch(IOException exception){
                System.out.println("couldn't save");
            }
        }).start();
    }
    public void stop(){
        if(stopped)
            return;
        this.stopped = true;
    }
}
