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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Instant;
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
    public NetXServer networkServer;
    private boolean stopped;
    public boolean paused;
    public boolean singleStep;
    private int tickCount;
    private int runningTickCount;
    private final UUID uuid;
    public final File saveFile;
    public final ArrayList<Vector3> scheduledExplosions;
    public String password;
    public int soundIdGenerator;
    public final ArrayList<PlayerTeam> teams;
    public HashMap<Integer,Float> currentCommunications;
    public HashMap<Integer,Float> backCommunications;
    public Server(File saveFile) {
        this.currentCommunications = new HashMap<>();
        this.backCommunications = new HashMap<>();
        this.soundIdGenerator = 0;
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
        this.networkServer = null;
        this.stopped = false;
        this.tickCount = 0;
        this.runningTickCount = 0;
        this.scheduledExplosions = new ArrayList<>();
        this.paused = false;
        this.singleStep = false;
        this.teams = new ArrayList<>();
        this.teams.add(new PlayerTeam("RED"));
        this.teams.add(new PlayerTeam("BLUE"));
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
                    ((GameObject) userDataA).onCollision(fixtureA, fixtureB, contact.getWorldManifold());
                }
                if(userDataB instanceof GameObject){
                    ((GameObject) userDataB).onCollision(fixtureB, fixtureA, contact.getWorldManifold());
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
    public void startNetwork(int port){
        if(this.networkServer != null)
            return;
        this.networkServer = new NetXServer(port, MessageRegistryCreator.create());
        this.networkServer.start();
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
    public static HashMap<String,Class> GAME_OBJECT_CLASSES = new HashMap<>();
    public static HashMap<Class,String> GAME_OBJECT_CLASSES_TYPES = new HashMap<>();
    static{
        GAME_OBJECT_CLASSES.put("frame", FrameGameObject.class);
        GAME_OBJECT_CLASSES.put("wheel", SimpleWheelGameObject.class);
        GAME_OBJECT_CLASSES.put("sticky_wheel", StickyWheelGameObject.class);
        GAME_OBJECT_CLASSES.put("cutting_wheel", CuttingWheelGameObject.class);
        GAME_OBJECT_CLASSES.put("balloon", BalloonGameObject.class);
        GAME_OBJECT_CLASSES.put("controller", ControllerGameObject.class);
        GAME_OBJECT_CLASSES.put("puncher", PunchBoxGameObject.class);
        GAME_OBJECT_CLASSES.put("propeller", PropellerGameObject.class);
        GAME_OBJECT_CLASSES.put("tnt", TntGameObject.class);
        GAME_OBJECT_CLASSES.put("rotator", RotatorGameObject.class);
        GAME_OBJECT_CLASSES.put("cannon", CannonGameObject.class);
        GAME_OBJECT_CLASSES.put("bullet", BulletGameObject.class);
        GAME_OBJECT_CLASSES.put("position_sensor", PositionSensorGameObject.class);
        GAME_OBJECT_CLASSES.put("distance_sensor", DistanceSensorGameObject.class);
        GAME_OBJECT_CLASSES.put("display", DisplayGameObject.class);
        GAME_OBJECT_CLASSES.put("math_unit", MathUnitGameObject.class);
        GAME_OBJECT_CLASSES.put("explosion_particle", ExplosionParticleGameObject.class);
        GAME_OBJECT_CLASSES.put("pid_controller", PIDControllerGameObject.class);
        GAME_OBJECT_CLASSES.put("weight", WeightGameObject.class);
        GAME_OBJECT_CLASSES.put("rope", RopeGameObject.class);
        GAME_OBJECT_CLASSES.put("grabber", GrabberGameObject.class);
        GAME_OBJECT_CLASSES.put("timer", TimerGameObject.class);
        GAME_OBJECT_CLASSES.put("stick", StickGameObject.class);
        GAME_OBJECT_CLASSES.put("piston", PistonGameObject.class);
        GAME_OBJECT_CLASSES.put("spring", SpringGameObject.class);
        GAME_OBJECT_CLASSES.put("jet_engine", JetEngineGameObject.class);
        GAME_OBJECT_CLASSES.put("fire_particle", FireParticleGameObject.class);
        GAME_OBJECT_CLASSES.put("flamethrower", FlamethrowerGameObject.class);
        GAME_OBJECT_CLASSES.put("motor", MotorGameObject.class);
        GAME_OBJECT_CLASSES.put("receiver", ReceiverGameObject.class);
        GAME_OBJECT_CLASSES.put("transmitter", TransmitterGameObject.class);

        for(Map.Entry<String, Class> entry : GAME_OBJECT_CLASSES.entrySet()){
            GAME_OBJECT_CLASSES_TYPES.put(entry.getValue(), entry.getKey());
        }
    }
    public GameObject spawnGameObject(Vector2 position, float rotation, String type, UUID uuid, GameObject.GameObjectConfig config){
        Class clazz = GAME_OBJECT_CLASSES.get(type);
        if(clazz == null)
            throw new IllegalArgumentException("unknown type " + type);
        try {
            Constructor<GameObject> constructor = clazz.getConstructor(Vector2.class, float.class, Server.class, GameObject.GameObjectConfig.class);
            GameObject gameObject = constructor.newInstance(position, rotation, this, config);
            if(uuid != null)
                gameObject.uuid = uuid;
            this.newGameObjects.add(gameObject);
            return gameObject;
        } catch (Exception e) {
            throw new IllegalStateException("failed to instantiate " + type, e);
        }
    }
    public EnumMap<EItemType, Float> getGameObjectCost(String type, GameObject.GameObjectConfig config){
        Class clazz = GAME_OBJECT_CLASSES.get(type);
        if(clazz == null)
            throw new IllegalArgumentException("unknown type " + type);
        try {
            Method method = clazz.getMethod("getItemCost", GameObject.GameObjectConfig.class);
            return (EnumMap<EItemType, Float>) method.invoke(null, config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public String getGameObjectId(GameObject gameObject){
        String id = GAME_OBJECT_CLASSES_TYPES.get(gameObject.getClass());
        if(id == null)
            throw new IllegalArgumentException("no id for gameobject " + gameObject.getClass().getSimpleName());
        return id;
    }
    public PlayerTeam getTeamByName(String name) {
        for(PlayerTeam team : teams){
            if(team.name.equals(name))
                return team;
        }
        return null;
    }
    private void addPlayer(Player player){
        this.players.add(player);
        this.newGameObjects.add(player);
        ArrayList<Object> messages = new ArrayList<>();
        this.clientWorldManager.addPlayer(player);
        player.send(this.terrain.createMessage());
        player.sendAll(messages);
        PlayerTeam team = this.teams.stream().min(Comparator.comparingInt(t -> t.players.size())).get();
        player.setTeam(team);
    }
    private void tick(float deltaTime) {
        HashMap<Integer,Float> swap = backCommunications;
        backCommunications = currentCommunications;
        currentCommunications = swap;
        currentCommunications.clear();
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
            for(int i = 0; i < INTERNAL_STEPS; i++) {
                for(GameObject gameObject : this.gameObjects.values()){
                    gameObject.internalTick();
                }
                this.physics.step(1.35f * deltaTime / INTERNAL_STEPS, 20, 20);
            }
        }
        for(Vector3 explosion : this.scheduledExplosions){
            Vector2 position = new Vector2(explosion.x, explosion.y);
            this.terrain.place("", position, explosion.z*2, false);
            Random random = new Random();
            for(int i = 0;i < 40;i++){
                ExplosionParticleGameObject go = spawnGameObject(position, 0f, ExplosionParticleGameObject::new, null, GameObject.GameObjectConfig.DEFAULT);
                go.power = explosion.z;
                go.getBaseBody().applyLinearImpulse(Vector2.Y.cpy().setAngleRad((float) (random.nextFloat()*Math.PI*2f)).scl(explosion.z*10*random.nextFloat()), go.getBaseBody().getWorldCenter(), true);
            }
        }
        this.scheduledExplosions.clear();
        this.clientWorldManager.updatePositions();
        this.players.removeIf(Player::isDisconnected);
        if(this.networkServer != null) {
            while (this.networkServer.visitMessage(new ServerMessage.Visitor() {
                @Override
                public void connect(SocketUser user) {
                    if (password == null) {
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
                    if (user.getUserData() != null)
                        ((Player) user.getUserData()).disconnect();
                }

                @Override
                public void message(SocketUser user, Object msg) {
                    if (user.getUserData() == null) {
                        if (msg instanceof SubmitPassword) {
                            SubmitPassword submitPasswordMessage = (SubmitPassword) msg;
                            if (password == null || submitPasswordMessage.password.equals(password)) {
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
            if(tickCount%TPS==1){
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
        }
        int autoSaveAfterTicks = TPS*60;
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
            if(!(gameObject instanceof Player) && !gameObject.isRemoved()) {
                if (gameObject == gameObject.vehicle.gameObjects.get(0)) {
                    saveFile.savedVehicles.add(gameObject.vehicle.save());
                }
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try {
                    gameObject.save(new DataOutputStream(outputStream));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                saveFile.savedGameObjects.add(new SaveFile.SavedGameObject(getGameObjectId(gameObject), gameObject.uuid, gameObject.getBaseBody().getPosition().cpy(), gameObject.getBaseBody().getAngle(), outputStream.toByteArray(), gameObject.config));
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
        for(SaveFile.SavedJoint joint : saveFile.savedJoints) {
            GameObject first = getGameObjectByUUID(joint.first);
            GameObject second = getGameObjectByUUID(joint.second);
            first.vehicle.add(second);
        }
        data.forEach((uuid1, bytes) -> {
            try {
                getGameObjectByUUID(uuid1).load(new DataInputStream(new ByteArrayInputStream(bytes)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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

        Body firstBase = first.getBody(first.getOpenConnections().get(firstName).connectionEdge.bodyName);
        Body secondBase = second.getBody(second.getOpenConnections().get(secondName).connectionEdge.bodyName);
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
    public static final int TPS = 60;
    public static final int INTERNAL_STEPS = 5;
    public void start(){
        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            while(!stopped){
                //long msptTimer = System.nanoTime();
                synchronized (physics) {
                    try {
                        tick(1f/TPS);
                    } catch (Exception e) {
                        e.printStackTrace();
                        stop();
                    }
                }
                //System.out.println("mspt: " + (System.nanoTime()-msptTimer)/1000000f);
                tickCount++;
                try {
                    int sleepTime = (int) (tickCount*(1000/((float)TPS))-(System.currentTimeMillis()-startTime));
                    if(sleepTime > 0)
                        Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if(this.networkServer != null)
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
