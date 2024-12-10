package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Align;
import com.github.industrialcraft.netx.NetXClient;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILink;
import com.github.industrialcraft.scrapbox.common.net.IConnection;
import com.github.industrialcraft.scrapbox.common.net.msg.*;
import com.github.industrialcraft.scrapbox.server.EItemType;
import com.github.industrialcraft.scrapbox.server.Server;
import com.github.industrialcraft.scrapbox.server.game.*;
import com.github.tommyettinger.colorful.rgb.ColorfulBatch;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

public class InGameScene implements IScene {
    public static final float BOX_TO_PIXELS_RATIO = 100;
    private ColorfulBatch batch;
    public CameraController cameraController;
    private Box2DDebugRenderer debugRenderer;
    public final IConnection connection;
    public final Server server;
    public final NetXClient client;
    public HashMap<Integer,ClientGameObject> gameObjects;
    public HashMap<String, RenderData> renderDataRegistry;
    public MouseSelector mouseSelector;
    private boolean debugRendering;
    private MouseSelector.Selection selected;
    public ToolBox toolBox;
    private ArrayList<ShowActivePossibleWelds.PossibleWeld> weldShowcase;
    private ShapeRenderer shapeRenderer;
    public TerrainRenderer terrainRenderer;
    public Stage stage;
    public HashMap<Integer,ClientGameObjectEditor> editors;
    public DragAndDrop dragAndDrop;
    private ControllingData controllingData;
    private ArrayList<SendConnectionListData.Connection> connectionsShowcase;
    private ArrayList<SendConnectionListData.GearConnection> gearConnectionsShowcase;
    private Texture jointBreakIcon;
    private boolean[] controllerState;
    private Texture controllerButton;
    private static final float JOINT_BREAK_ICON_SIZE = 48;
    private static final float CONTROLLER_BUTTON_SIZE = 80;
    public Dialog escapeMenu;
    private TextureRegion puncherSpringTexture;
    private TextureRegion springTexture;
    private boolean isPaused;
    private TextureRegion pausedTexture;
    private TextureRegion grabberStickyTexture;
    private BuildAreaRenderer buildAreaRenderer;
    private int gearJointSelection;
    public HashMap<String, Sound> sounds;
    public HashMap<Integer, ClientSoundInstance> soundInstances;
    public EnumMap<EItemType,Float> inventory;
    public EnumMap<EItemType,Texture> itemTextures;
    public boolean infiniteItems;
    public ArrayList<String> teams;
    public String playerTeam;
    public InGameScene(IConnection connection, Server server, NetXClient client) {
        this.connection = connection;
        this.server = server;
        this.client = client;
    }
    @Override
    public void create() {
        this.itemTextures = new EnumMap<>(EItemType.class);
        this.itemTextures.put(EItemType.Wood, new Texture("item_wood.png"));
        this.itemTextures.put(EItemType.Metal, new Texture("item_metal.png"));
        this.itemTextures.put(EItemType.Circuit, new Texture("item_circuit.png"));
        this.itemTextures.put(EItemType.Transmission, new Texture("item_transmission.png"));
        this.itemTextures.put(EItemType.StickyResin, new Texture("item_sticky_resin.png"));
        this.itemTextures.put(EItemType.Explosive, new Texture("item_explosive.png"));
        this.inventory = new EnumMap<>(EItemType.class);
        this.infiniteItems = false;
        this.gearJointSelection = -1;
        this.dragAndDrop = new DragAndDrop();
        sounds = new HashMap<>();
        sounds.put("wood_impact", Gdx.audio.newSound(Gdx.files.internal("wood_impact.wav")));
        sounds.put("metal_impact", Gdx.audio.newSound(Gdx.files.internal("metal_impact.wav")));
        sounds.put("explosion", Gdx.audio.newSound(Gdx.files.internal("explosion_medium.wav")));
        stage = new Stage();
        editors = new HashMap<>();
        soundInstances = new HashMap<>();
        cameraController = new CameraController(this, new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        debugRenderer = new Box2DDebugRenderer();
        puncherSpringTexture = new TextureRegion(new Texture("puncher_spring.png"));
        springTexture = new TextureRegion(new Texture("spring.png"));
        pausedTexture = new TextureRegion(new Texture("paused.png"));
        grabberStickyTexture = new TextureRegion(new Texture("grabber_sticky.png"));
        buildAreaRenderer = new BuildAreaRenderer();
        renderDataRegistry = new HashMap<>();
        renderDataRegistry.put("frame", new RenderData(null, 1, 1).addMaterialTexture(new Texture("frame.png")));
        renderDataRegistry.put("rope", new RenderData(new Texture("rope.png"), 1, 1));//only icon
        renderDataRegistry.put("spring", new RenderData(new Texture("spring.png"), 1, 1));//only icon
        renderDataRegistry.put("spring_start", new RenderData(new Texture("spring_end.png"), 0.125f, 0.5f, (renderData, gameObject, batch1) -> {
            float animation = -gameObject.getAnimationNumber("length", 0);
            Vector2 lerpedPosition = gameObject.getRealPosition();
            lerpedPosition.add(new Vector2(1, -0.5f).rotateRad(gameObject.getRealAngle()));
            batch.draw(springTexture, (lerpedPosition.x - 1) * InGameScene.BOX_TO_PIXELS_RATIO, (lerpedPosition.y - 1) * InGameScene.BOX_TO_PIXELS_RATIO, 1 * InGameScene.BOX_TO_PIXELS_RATIO, 1 * InGameScene.BOX_TO_PIXELS_RATIO, 1 * InGameScene.BOX_TO_PIXELS_RATIO, animation * InGameScene.BOX_TO_PIXELS_RATIO, 1, 1, (float) Math.toDegrees(gameObject.getRealAngle() - Math.PI/2));
            renderData.draw(batch1, gameObject);
        }));
        renderDataRegistry.put("spring_end", new RenderData(new Texture("spring_end.png"), 0.125f, 0.5f));
        RenderData.CustomRenderFunction ropeRenderer = (renderData, gameObject, batch1) -> {
            float length = gameObject.getAnimationNumber("length", 3);
            int otherId = Integer.parseInt(gameObject.getAnimationString("other", "0"));
            renderData.draw(batch1, gameObject);
            if(otherId > gameObject.id){
                if(!gameObjects.containsKey(otherId)) {
                    return;
                }
                batch1.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(217f/255f, 160f/255f, 100f/255f, 1f);
                ClientGameObject otherGo = gameObjects.get(otherId);
                Vector2 firstOffset = new Vector2(gameObject.getAnimationNumber("offsetX", 5), gameObject.getAnimationNumber("offsetY", 5)).rotateRad(gameObject.getRealAngle());
                Vector2 secondOffset = new Vector2(otherGo.getAnimationNumber("offsetX", 5), otherGo.getAnimationNumber("offsetY", 5)).rotateRad(otherGo.getRealAngle());
                Vector2 first = gameObject.getRealPosition().add(firstOffset);
                Vector2 second = otherGo.getRealPosition().add(secondOffset);
                Bezier<Vector2> bezier = new Bezier<>(first, new Vector2((first.x+second.x)/2f, (first.y+second.y)/2f-(length-first.dst(second))), second);
                Vector2 previous = gameObject.getRealPosition().add(firstOffset);
                for(float n = 0;n < 1;n+=0.01f) {
                    Vector2 point = bezier.valueAt(new Vector2(), n);
                    shapeRenderer.rectLine(previous.scl(BOX_TO_PIXELS_RATIO),point.cpy().scl(BOX_TO_PIXELS_RATIO), 3);
                    previous = point;
                }

                shapeRenderer.end();
                batch1.begin();
            }
        };
        renderDataRegistry.put("jet_engine", new RenderData(new Texture("jet_engine.png"), 1f, 1f, ropeRenderer));
        renderDataRegistry.put("rope_connector", new RenderData(new Texture("rope_connector.png"), 0.2f, 0.2f, ropeRenderer));
        renderDataRegistry.put("stick", new RenderData(new Texture("stick.png"), 1, 1));//only icon
        renderDataRegistry.put("stick_connector", new RenderData(new Texture("rope_connector.png"), 0.2f, 0.2f, (renderData, gameObject, batch1) -> {
            int otherId = Integer.parseInt(gameObject.getAnimationString("other", "0"));
            renderData.draw(batch1, gameObject);
            if(otherId > gameObject.id){
                if(!gameObjects.containsKey(otherId)) {
                    return;
                }
                batch1.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(100f/255f, 100f/255f, 100f/255f, 1f);
                shapeRenderer.rectLine(gameObject.getRealPosition().scl(BOX_TO_PIXELS_RATIO),gameObjects.get(otherId).getRealPosition().scl(BOX_TO_PIXELS_RATIO), 3);
                shapeRenderer.end();
                batch1.begin();
            }
        }));
        renderDataRegistry.put("wheel", new RenderData(null, 1, 1).setConfigScalingEnabled(true, true).addMaterialTexture(new Texture("wheel.png")));
        renderDataRegistry.put("cutting_wheel", new RenderData(new Texture("cutting_wheel.png"), 1, 1).setConfigScalingEnabled(true, true).addMaterialTexture(new Texture("wheel.png")));
        renderDataRegistry.put("sticky_wheel", new RenderData(new Texture("sticky_wheel.png"), 1, 1).setConfigScalingEnabled(true, true).addMaterialTexture(new Texture("wheel.png")));
        renderDataRegistry.put("wheel_join", new RenderData(null, 1, 1).setConfigScalingEnabled(false, true).addMaterialTexture(new Texture("wheel_join.png")));
        renderDataRegistry.put("balloon", new RenderData(new Texture("balloon.png"), 1, 1, ropeRenderer));
        renderDataRegistry.put("puncher_box", new RenderData(new Texture("puncher_box.png"), FrameGameObject.INSIDE_SIZE, FrameGameObject.INSIDE_SIZE, (renderData, gameObject, batch1) -> {
            float animation = gameObject.getAnimationNumber("animation", 0);
            Vector2 lerpedPosition = gameObject.getRealPosition();
            lerpedPosition.add(new Vector2(0, 1f).rotateRad(gameObject.getRealAngle()));
            batch.draw(puncherSpringTexture, (lerpedPosition.x - 1) * InGameScene.BOX_TO_PIXELS_RATIO, (lerpedPosition.y - 1) * InGameScene.BOX_TO_PIXELS_RATIO, 1 * InGameScene.BOX_TO_PIXELS_RATIO, 1 * InGameScene.BOX_TO_PIXELS_RATIO, 1 * InGameScene.BOX_TO_PIXELS_RATIO * 2, 1 * 2f * animation * InGameScene.BOX_TO_PIXELS_RATIO * 2, 1, 1, (float) Math.toDegrees(gameObject.getRealAngle()));
            renderData.draw(batch1, gameObject);
        }));
        renderDataRegistry.put("puncher", new RenderData(new Texture("puncher.png"), 1, 1, (renderData, gameObject, batch1) -> {
            if(gameObject.getAnimationNumber("animation", 0) > 0.1) {
                renderData.draw(batch, gameObject);
            }
        }));
        renderDataRegistry.put("controller", new RenderData(new Texture("controller.png"), FrameGameObject.INSIDE_SIZE, FrameGameObject.INSIDE_SIZE));
        renderDataRegistry.put("timer", new RenderData(new Texture("timer.png"), FrameGameObject.INSIDE_SIZE, FrameGameObject.INSIDE_SIZE));
        renderDataRegistry.put("weight", new RenderData(new Texture("weight.png"), FrameGameObject.INSIDE_SIZE, FrameGameObject.INSIDE_SIZE));
        renderDataRegistry.put("distance_sensor", new RenderData(new Texture("distance_sensor.png"), 0.45f, 0.15f, (renderData, gameObject, batch1) -> {
            renderData.draw(batch1, gameObject);
            batch1.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1, 0, 0, 1f);
            Vector2 rotation = new Vector2(1, 0).setAngleRad((float) (gameObject.getRealAngle()+Math.PI/2));
            Vector2 target = gameObject.getRealPosition().add(rotation.scl(gameObject.getAnimationNumber("length", 10))).scl(InGameScene.BOX_TO_PIXELS_RATIO);
            shapeRenderer.rectLine(gameObject.getRealPosition().scl(InGameScene.BOX_TO_PIXELS_RATIO), target, 5);
            if(gameObject.getAnimationNumber("length", 10) < gameObject.getAnimationNumber("max", 10)){
                shapeRenderer.circle(target.x, target.y, 10);
            }
            shapeRenderer.end();
            batch1.begin();
        }));
        renderDataRegistry.put("propeller", new RenderData(new Texture("propeller.png"), 1, 0.25f, (renderData, gameObject, batch1) -> {
            Vector2 lerpedPosition = gameObject.getRealPosition();
            float time = gameObject.internalRendererData!=null? (float) gameObject.internalRendererData :0f;
            if(Float.isNaN(time)) {
                time = 0;
            }
            float speed = gameObject.getAnimationNumber("speed", 0);
            time += Gdx.graphics.getDeltaTime() * Math.max(speed * 30, time != 0?5:0);
            if(speed == 0 && Math.cos(time) > 0.9 && Math.cos(time) < 1.){
                time = 0;
            }
            float realWidth = (float) (renderData.width * Math.cos(time));
            gameObject.internalRendererData = time;
            batch.draw(renderData.texture, (lerpedPosition.x - realWidth) * InGameScene.BOX_TO_PIXELS_RATIO, (lerpedPosition.y - renderData.height) * InGameScene.BOX_TO_PIXELS_RATIO, realWidth * InGameScene.BOX_TO_PIXELS_RATIO, renderData.height * InGameScene.BOX_TO_PIXELS_RATIO, realWidth * InGameScene.BOX_TO_PIXELS_RATIO * 2, renderData.height * InGameScene.BOX_TO_PIXELS_RATIO * 2, 1, 1, (float) Math.toDegrees(gameObject.getRealAngle()));
        }));
        renderDataRegistry.put("grabber", new RenderData(new Texture("grabber.png"), 1, 0.1875f, (renderData, gameObject, batch1) -> {
            renderData.draw(batch1, gameObject);
            if(gameObject.getAnimationNumber("suction", 0) > 0) {
                Vector2 lerpedPosition = gameObject.getRealPosition();
                Vector2 offset = new Vector2(renderData.height*2f, 0f);
                offset.setAngleRad((float) (gameObject.getRealAngle() + Math.PI/2));
                batch.draw(grabberStickyTexture, (lerpedPosition.x - renderData.width + offset.x) * InGameScene.BOX_TO_PIXELS_RATIO, (lerpedPosition.y - renderData.height + offset.y) * InGameScene.BOX_TO_PIXELS_RATIO, renderData.width * InGameScene.BOX_TO_PIXELS_RATIO, renderData.height * InGameScene.BOX_TO_PIXELS_RATIO, renderData.width * InGameScene.BOX_TO_PIXELS_RATIO * 2, 0.0625f * InGameScene.BOX_TO_PIXELS_RATIO * 2, 1, 1, (float) Math.toDegrees(gameObject.getRealAngle()));
            }
        }));
        renderDataRegistry.put("tnt", new RenderData(new Texture("tnt.png"), 1, 1));
        renderDataRegistry.put("piston_box", new RenderData(new Texture("piston_box.png"), FrameGameObject.INSIDE_SIZE, FrameGameObject.INSIDE_SIZE, (renderData, gameObject, batch1) -> {
            float animation = gameObject.getAnimationNumber("length", 0);
            Vector2 lerpedPosition = gameObject.getRealPosition();
            lerpedPosition.add(new Vector2(0, 1f).rotateRad(gameObject.getRealAngle()));
            batch.draw(puncherSpringTexture, (lerpedPosition.x - 1) * InGameScene.BOX_TO_PIXELS_RATIO, (lerpedPosition.y - 1) * InGameScene.BOX_TO_PIXELS_RATIO, 1 * InGameScene.BOX_TO_PIXELS_RATIO, 1 * InGameScene.BOX_TO_PIXELS_RATIO, 1 * InGameScene.BOX_TO_PIXELS_RATIO * 2, (animation + 1) * InGameScene.BOX_TO_PIXELS_RATIO, 1, 1, (float) Math.toDegrees(gameObject.getRealAngle()));
            renderData.draw(batch1, gameObject);
        }));
        renderDataRegistry.put("piston_end", new RenderData(new Texture("piston_end.png"), 1, 0.125f));
        renderDataRegistry.put("rotator_join", new RenderData(new Texture("rotator_join.png"), 1, 1));
        renderDataRegistry.put("rotator_end", new RenderData(new Texture("rotator_end.png"), 1, 1));
        renderDataRegistry.put("cannon", new RenderData(new Texture("cannon.png"), 1, 1));
        renderDataRegistry.put("bullet", new RenderData(new Texture("bullet.png"), 0.1f, 0.1f));
        renderDataRegistry.put("position_sensor", new RenderData(new Texture("position_sensor.png"), FrameGameObject.INSIDE_SIZE, FrameGameObject.INSIDE_SIZE));
        renderDataRegistry.put("math_unit", new RenderData(new Texture("math_unit.png"), FrameGameObject.INSIDE_SIZE, FrameGameObject.INSIDE_SIZE));
        renderDataRegistry.put("explosion_particle", new RenderData(new Texture("explosion_particle.png"), 0.5f, 0.5f));
        renderDataRegistry.put("fire_particle", new RenderData(new Texture("fire_particle.png"), 0.5f, 0.5f));
        renderDataRegistry.put("flamethrower", new RenderData(new Texture("flamethrower.png"), 1f, 1f));
        renderDataRegistry.put("motor", new RenderData(new Texture("motor.png"), FrameGameObject.INSIDE_SIZE, FrameGameObject.INSIDE_SIZE));
        renderDataRegistry.put("pid_controller", new RenderData(new Texture("pid_controller.png"), FrameGameObject.INSIDE_SIZE, FrameGameObject.INSIDE_SIZE));
        renderDataRegistry.put("receiver", new RenderData(new Texture("receiver.png"), 0.55f, 0.75f));
        renderDataRegistry.put("transmitter", new RenderData(new Texture("transmitter.png"), 0.6f, 0.45f));
        renderDataRegistry.put("player", new RenderData(new Texture("player.png"), 0.5f, 0.5f, (renderData, gameObject, batch1) -> {
            Vector2 lerpedPosition = gameObject.getRealPosition();
            int color = Integer.parseInt(gameObject.getAnimationString("color", "000000"), 16);
            int r = color & 255;
            int g = (color >> 8) & 255;
            int b = (color >> 16) & 255;
            batch.setColor(r*0.5f, g*0.5f, b*0.5f, 1f);
            batch.draw(renderData.texture, (lerpedPosition.x - renderData.width) * InGameScene.BOX_TO_PIXELS_RATIO, (lerpedPosition.y - renderData.height) * InGameScene.BOX_TO_PIXELS_RATIO, renderData.width * InGameScene.BOX_TO_PIXELS_RATIO, renderData.height * InGameScene.BOX_TO_PIXELS_RATIO, renderData.width * InGameScene.BOX_TO_PIXELS_RATIO * 2, renderData.height * InGameScene.BOX_TO_PIXELS_RATIO * 2, 1, 1, (float) Math.toDegrees(gameObject.getRealAngle()));
        }));
        renderDataRegistry.put("display", new RenderData(new Texture("display.png"), FrameGameObject.INSIDE_SIZE, FrameGameObject.INSIDE_SIZE, (renderData, gameObject, batch) -> {
            renderData.draw(batch, gameObject);
            Matrix4 mx4Font = new Matrix4();
            Vector3 translation = new Vector3(gameObject.getRealPosition().x * BOX_TO_PIXELS_RATIO, gameObject.getRealPosition().y * BOX_TO_PIXELS_RATIO, 0);
            mx4Font.translate(translation).rotateRad(new Vector3(0, 0, 1), gameObject.rotation).translate(translation.cpy().scl(-1));
            batch.setTransformMatrix(mx4Font);
            GlyphLayout layout = new GlyphLayout();
            String text = gameObject.getAnimationString("text", "");
            BitmapFont font = ScrapBox.getInstance().getSkin().getFont("default-font");
            font.setColor(Color.WHITE);
            layout.setText(font, text);
            float realWidth = Math.min(layout.width, FrameGameObject.INSIDE_SIZE*2);
            font.draw(batch, text, translation.x - layout.width / 2, translation.y + layout.height/2);
            mx4Font.idt();
            batch.setTransformMatrix(mx4Font);
        }));
        batch = new ColorfulBatch();
        gameObjects = new HashMap<>();
        debugRendering = false;
        mouseSelector = new MouseSelector(this);
        this.toolBox = new ToolBox(this);
        this.toolBox.addPart("frame", renderDataRegistry.get("frame"), FrameGameObject::getItemCost, true, false);
        this.toolBox.addPart("wheel", renderDataRegistry.get("wheel"), SimpleWheelGameObject::getItemCost, true, true);
        this.toolBox.addPart("sticky_wheel", renderDataRegistry.get("sticky_wheel"), StickyWheelGameObject::getItemCost, true, true);
        this.toolBox.addPart("cutting_wheel", renderDataRegistry.get("cutting_wheel"), CuttingWheelGameObject::getItemCost, true, true);
        this.toolBox.addPart("motor", renderDataRegistry.get("motor"), MotorGameObject::getItemCost, false, false);
        this.toolBox.addPart("balloon", renderDataRegistry.get("balloon"), BalloonGameObject::getItemCost, false, false);
        this.toolBox.addPart("controller", renderDataRegistry.get("controller"), ControllerGameObject::getItemCost, false, false);
        this.toolBox.addPart("puncher", renderDataRegistry.get("puncher_box"), PunchBoxGameObject::getItemCost, false, false);
        this.toolBox.addPart("propeller", renderDataRegistry.get("propeller"), PropellerGameObject::getItemCost, false, false);
        this.toolBox.addPart("tnt", renderDataRegistry.get("tnt"), TntGameObject::getItemCost, false, false);
        this.toolBox.addPart("rotator", renderDataRegistry.get("rotator_join"), RotatorGameObject::getItemCost, false, false);
        this.toolBox.addPart("cannon", renderDataRegistry.get("cannon"), CannonGameObject::getItemCost, false, false);
        this.toolBox.addPart("position_sensor", renderDataRegistry.get("position_sensor"), PositionSensorGameObject::getItemCost, false, false);
        this.toolBox.addPart("display", renderDataRegistry.get("display"), DisplayGameObject::getItemCost, false, false);
        this.toolBox.addPart("math_unit", renderDataRegistry.get("math_unit"), MathUnitGameObject::getItemCost, false, false);
        this.toolBox.addPart("distance_sensor", renderDataRegistry.get("distance_sensor"), DistanceSensorGameObject::getItemCost, false, false);
        this.toolBox.addPart("pid_controller", renderDataRegistry.get("pid_controller"), PIDControllerGameObject::getItemCost, false, false);
        this.toolBox.addPart("weight", renderDataRegistry.get("weight"), WeightGameObject::getItemCost, false, false);
        this.toolBox.addPart("rope", renderDataRegistry.get("rope"), RopeGameObject::getItemCost, false, false);
        this.toolBox.addPart("stick", renderDataRegistry.get("stick"), StickGameObject::getItemCost, false, false);
        this.toolBox.addPart("grabber", renderDataRegistry.get("grabber"), GrabberGameObject::getItemCost,false, false);
        this.toolBox.addPart("timer", renderDataRegistry.get("timer"), TimerGameObject::getItemCost, false, false);
        this.toolBox.addPart("piston", renderDataRegistry.get("piston_box"), PistonGameObject::getItemCost, false, false);
        this.toolBox.addPart("spring", renderDataRegistry.get("spring"), SpringGameObject::getItemCost, false, false);
        this.toolBox.addPart("jet_engine", renderDataRegistry.get("jet_engine"), JetEngineGameObject::getItemCost, false, false);
        this.toolBox.addPart("flamethrower", renderDataRegistry.get("flamethrower"), FlamethrowerGameObject::getItemCost, false, false);
        this.toolBox.addPart("receiver", renderDataRegistry.get("receiver"), ReceiverGameObject::getItemCost, false, false);
        this.toolBox.addPart("transmitter", renderDataRegistry.get("transmitter"), TransmitterGameObject::getItemCost, false, false);
        this.weldShowcase = new ArrayList<>();
        this.shapeRenderer = new ShapeRenderer();
        this.terrainRenderer = new TerrainRenderer();
        addTerrainType("", "terrain_air.png");
        addTerrainType("dirt", "dirt.png");
        addTerrainType("stone", "stone.png");
        addTerrainType("ice", "ice.png");
        this.connectionsShowcase = new ArrayList<>();
        this.gearConnectionsShowcase = new ArrayList<>();
        this.jointBreakIcon = new Texture("joint_break_icon.png");
        this.controllerState = new boolean[10];
        this.controllerButton = new Texture("controller_button.png");
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, new InputProcessor() {
            @Override
            public boolean keyDown(int keycode) {
                if(keycode == ScrapBox.getSettings().GHOST_MODE.key){
                    connection.send(new PinchingGhostToggle());
                }
                if(controllingData != null) {
                    if (keycode >= Input.Keys.NUM_1 && keycode <= Input.Keys.NUM_9) {
                        connection.send(new ControllerInput(controllingData.controllingId, keycode - Input.Keys.NUM_1, true));
                    }
                    if(keycode == Input.Keys.NUM_0){
                        connection.send(new ControllerInput(controllingData.controllingId, 9, true));
                    }
                }
                return false;
            }
            @Override
            public boolean keyUp(int keycode) {
                if(controllingData != null) {
                    if (keycode >= Input.Keys.NUM_1 && keycode <= Input.Keys.NUM_9) {
                        connection.send(new ControllerInput(controllingData.controllingId, keycode - Input.Keys.NUM_1, false));
                    }
                    if(keycode == Input.Keys.NUM_0){
                        connection.send(new ControllerInput(controllingData.controllingId, 9, false));
                    }
                }
                return false;
            }
            @Override
            public boolean keyTyped(char character) {
                return false;
            }
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if(!toolBox.isMouseInside()) {
                    if(button == Input.Buttons.LEFT) {
                        if (toolBox.tool == ToolBox.Tool.Hand && !(ScrapBox.getSettings().BREAK_CONNECTION.isDown() || ScrapBox.getSettings().GEAR_CONNECTION.isDown())) {
                            selected = mouseSelector.getSelected();
                            if (selected != null) {
                                connection.send(new GameObjectPinch(selected.selectionId, new Vector2(selected.offsetX, selected.offsetY)));
                            }
                        }
                        if (toolBox.tool == ToolBox.Tool.Hand && ScrapBox.getSettings().BREAK_CONNECTION.isDown()) {
                            Vector2 mouse2 = mouseSelector.getWorldMousePosition().scl(BOX_TO_PIXELS_RATIO);
                            if (ScrapBox.getSettings().GEAR_CONNECTION.isDown()) {
                                for (SendConnectionListData.GearConnection connection1 : gearConnectionsShowcase) {
                                    ClientGameObject objectA = gameObjects.get(connection1.goA);
                                    ClientGameObject objectB = gameObjects.get(connection1.goB);
                                    if (objectA != null && objectB != null) {
                                        Vector2 position = objectA.getRealPosition().lerp(objectB.getRealPosition(), 0.5f).scl(BOX_TO_PIXELS_RATIO);
                                        if (mouse2.dst(position) < JOINT_BREAK_ICON_SIZE / 2) {
                                            connection.send(new DestroyGearConnection(objectA.id, objectB.id));
                                        }
                                    }
                                }
                            } else {
                                for (SendConnectionListData.Connection connection1 : connectionsShowcase) {
                                    if (mouse2.dst(connection1.position.cpy().scl(BOX_TO_PIXELS_RATIO)) < JOINT_BREAK_ICON_SIZE / 2) {
                                        connection.send(new DestroyJoint(connection1.gameObjectId, connection1.name));
                                    }
                                }
                            }
                        }
                        if (toolBox.tool == ToolBox.Tool.Hand && ScrapBox.getSettings().GEAR_CONNECTION.isDown() && !ScrapBox.getSettings().BREAK_CONNECTION.isDown()) {
                            MouseSelector.Selection selection = mouseSelector.getSelected(clientGameObject -> clientGameObject.gearJoinable);
                            if (selection != null) {
                                gearJointSelection = selection.selectionId;
                            }
                        }
                        if (toolBox.tool == ToolBox.Tool.TerrainModify) {
                            connection.send(new PlaceTerrain(toolBox.getSelectedTerrainType(), mouseSelector.getWorldMousePosition(), 2 * toolBox.brushSize, toolBox.brushRectangle));
                        }
                    }
                } else {
                    if(button == Input.Buttons.LEFT || button == Input.Buttons.RIGHT)
                        toolBox.click(new Vector2(screenX, Gdx.graphics.getHeight()-screenY), button == Input.Buttons.RIGHT);
                }
                return false;
            }
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if(button == Input.Buttons.LEFT) {
                    if (selected != null) {
                        connection.send(new GameObjectRelease());
                        if (toolBox.isMouseInside()) {
                            connection.send(new TrashObject(selected.selectionId));
                        }
                    }
                    if (ScrapBox.getSettings().GEAR_CONNECTION.isDown() && gearJointSelection != -1) {
                        MouseSelector.Selection sel = mouseSelector.getSelected();
                        if (sel != null) {
                            TextField ratioA = new TextField("1", ScrapBox.getInstance().getSkin());
                            TextField.TextFieldFilter fieldFilter = (textField, c) -> Character.isDigit(c) || c == '-';
                            ratioA.setTextFieldFilter(fieldFilter);
                            TextField ratioB = new TextField("1", ScrapBox.getInstance().getSkin());
                            ratioB.setTextFieldFilter(fieldFilter);
                            Dialog gearRatio = new Dialog("Enter gear ratio", ScrapBox.getInstance().getSkin(), "dialog") {
                                @Override
                                protected void result(Object object) {
                                    if (object instanceof String) {
                                        try {
                                            int rA = ratioA.getText().isEmpty() ? 1 : Integer.parseInt(ratioA.getText());
                                            int rB = ratioB.getText().isEmpty() ? 1 : Integer.parseInt(ratioB.getText());
                                            connection.send(new CreateGearConnection(gearJointSelection, rA, sel.id, rB));
                                        } catch (Exception e) {
                                        }
                                    }
                                    gearJointSelection = -1;
                                }
                            };
                            gearRatio.setMovable(false);
                            Table table = gearRatio.getContentTable();
                            table.add(new Label("Ratio A: ", ScrapBox.getInstance().getSkin()), ratioA).row();
                            table.add(new Label("Ratio B: ", ScrapBox.getInstance().getSkin()), ratioB).row();
                            gearRatio.button("Ok", "");
                            gearRatio.button("Back");
                            gearRatio.show(stage);
                        } else {
                            gearJointSelection = -1;
                        }
                    } else {
                        gearJointSelection = -1;
                    }
                    selected = null;
                }
                return false;
            }
            @Override
            public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
                return false;
            }
            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if(!toolBox.isMouseInside()) {
                    if (toolBox.tool == ToolBox.Tool.TerrainModify) {
                        connection.send(new PlaceTerrain(toolBox.getSelectedTerrainType(), mouseSelector.getWorldMousePosition(), 2 * toolBox.brushSize, toolBox.brushRectangle));
                    }
                }
                return false;
            }
            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                return false;
            }
            @Override
            public boolean scrolled(float amountX, float amountY) {
                if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)){
                    if(toolBox.isTerrainSelectionOpen()){
                        toolBox.changeBrushSize(amountY);
                    } else {
                        connection.send(new PinchingRotate(amountY));
                    }
                } else if(toolBox.isMouseInside()){
                    toolBox.scroll((int) amountY);
                } else {
                    cameraController.zoom(amountY);
                }
                return false;
            }
        }));
    }
    private void addTerrainType(String type, String texture){
        this.terrainRenderer.addTerrainType(type, texture);
        this.toolBox.addTerrainType(type);
    }
    @Override
    public void render() {
        Gdx.gl.glClearColor(79f / 255f, 201f / 255f, 232f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        for(Object message : connection.read()){
            if(message instanceof DisconnectMessage) {
                ScrapBox.getInstance().setScene(new DisconnectedScene(((DisconnectMessage) message).reason));
                return;
            }
            if(message instanceof GamePausedState){
                GamePausedState gamePausedStateMessage = (GamePausedState) message;
                isPaused = gamePausedStateMessage.paused;
            }
            if(message instanceof AddGameObjectMessage){
                AddGameObjectMessage addGameObjectMessage = (AddGameObjectMessage) message;
                gameObjects.put(addGameObjectMessage.id, new ClientGameObject(addGameObjectMessage));
            }
            if(message instanceof MoveGameObjectMessage){
                MoveGameObjectMessage moveGameObjectMessage = (MoveGameObjectMessage) message;
                ClientGameObject gameObject = gameObjects.get(moveGameObjectMessage.id);
                if(gameObject != null){
                    gameObject.move(moveGameObjectMessage);
                }
            }
            if(message instanceof DeleteGameObject){
                DeleteGameObject deleteGameObject = (DeleteGameObject) message;
                ClientGameObject gameObject = gameObjects.remove(deleteGameObject.id);
                ClientGameObjectEditor editor = editors.get(deleteGameObject.id);
                if(editor != null)
                    closeEditor(editor);
                soundInstances.values().forEach(clientSoundInstance -> {
                    if(clientSoundInstance.gameObject == gameObject)
                        clientSoundInstance.stop();
                });
            }
            if(message instanceof TakeObjectResponse){
                TakeObjectResponse takeObjectResponse = (TakeObjectResponse) message;
                selected = new MouseSelector.Selection(takeObjectResponse.id, takeObjectResponse.id, takeObjectResponse.offset.x, takeObjectResponse.offset.y, 0);
                connection.send(new GameObjectPinch(selected.id, new Vector2(selected.offsetX, selected.offsetY)));
            }
            if(message instanceof ShowActivePossibleWelds){
                ShowActivePossibleWelds showActivePossibleWelds = (ShowActivePossibleWelds) message;
                this.weldShowcase = showActivePossibleWelds.welds;
            }
            if(message instanceof TerrainShapeMessage){
                TerrainShapeMessage terrainShapeMessage = (TerrainShapeMessage) message;
                this.terrainRenderer.loadMessage(terrainShapeMessage);
            }
            if(message instanceof SetGameObjectEditUIData){
                SetGameObjectEditUIData setGameObjectEditUIData = (SetGameObjectEditUIData) message;
                ClientGameObjectEditor editor = this.editors.get(setGameObjectEditUIData.id);
                if(editor == null){
                    editor = new ClientGameObjectEditor(setGameObjectEditUIData.id, this, setGameObjectEditUIData);
                    stage.addActor(editor.window);
                    this.editors.put(setGameObjectEditUIData.id, editor);
                } else {
                    editor.rebuild(setGameObjectEditUIData);
                }
            }
            if(message instanceof SendConnectionListData){
                SendConnectionListData sendConnectionListData = (SendConnectionListData) message;
                this.connectionsShowcase = sendConnectionListData.connections;
                this.gearConnectionsShowcase = sendConnectionListData.gearConnections;
            }
            if(message instanceof ResponseControllerState){
                ResponseControllerState responseControllerState = (ResponseControllerState) message;
                this.controllerState = responseControllerState.state;
            }
            if(message instanceof UpdateBuildableAreas){
                UpdateBuildableAreas updateBuildableAreas = (UpdateBuildableAreas) message;
                buildAreaRenderer.buildableAreas = updateBuildableAreas.areas;
            }
            if(message instanceof PlaySoundMessage){
                PlaySoundMessage playSoundMessage = (PlaySoundMessage) message;
                ClientSoundInstance soundInstance = new ClientSoundInstance(this, playSoundMessage);
                soundInstances.put(soundInstance.serverId, soundInstance);
            }
            if(message instanceof UpdateInventory){
                UpdateInventory updateInventory = (UpdateInventory) message;
                this.inventory = updateInventory.inventory;
                this.infiniteItems = updateInventory.infinite;
            }
            if(message instanceof PlayerTeamUpdate){
                PlayerTeamUpdate playerTeamUpdateMessage = (PlayerTeamUpdate) message;
                this.playerTeam = playerTeamUpdateMessage.team;
            }
            if(message instanceof PlayerTeamList){
                PlayerTeamList playerTeamListMessage = (PlayerTeamList) message;
                this.teams = playerTeamListMessage.teams;
            }
        }
        if(ScrapBox.getSettings().PAUSE_GAME.isJustDown()){
            connection.send(new ToggleGamePaused(false));
        }
        if(ScrapBox.getSettings().STEP_GAME.isJustDown()){
            connection.send(new ToggleGamePaused(true));
        }
        if(controllingData != null){
            connection.send(new RequestControllerState(controllingData.controllingId));
            ClientGameObject gameObject = gameObjects.get(controllingData.controllingId);
            if(gameObject == null){
                controllingData = null;
            } else {
                Vector2 lerpedPosition = gameObject.getRealPosition();
                cameraController.camera.position.set(lerpedPosition.x * BOX_TO_PIXELS_RATIO, lerpedPosition.y * BOX_TO_PIXELS_RATIO, 0);
            }
        } else {
            cameraController.tick();
        }
        soundInstances.values().forEach(clientSoundInstance -> clientSoundInstance.tick(cameraController));
        soundInstances.values().removeIf(ClientSoundInstance::isStopped);
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            if(escapeMenu == null) {
                escapeMenu = new Dialog("Escape Menu", ScrapBox.getInstance().getSkin(), "dialog"){
                    @Override
                    protected void result(Object object) {
                        InGameScene.this.escapeMenu = null;
                    }
                };
                escapeMenu.setMovable(false);
                Table table = escapeMenu.getContentTable();
                TextButton settings = new TextButton("Settings", ScrapBox.getInstance().getSkin());
                settings.addListener(new ClickListener(){
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        escapeMenu.remove();
                        Dialog settingsWindow = new Dialog("Settings", ScrapBox.getInstance().getSkin(), "dialog"){
                            @Override
                            protected void result(Object object) {
                                escapeMenu.remove();
                                escapeMenu = null;
                            }
                        };
                        settingsWindow.getContentTable().add(ScrapBox.getSettings().createTable(() -> {
                            settingsWindow.pack();
                        }));
                        settingsWindow.button("Close");
                        settingsWindow.show(stage);
                        escapeMenu = settingsWindow;
                    }
                });
                table.add(settings).row();
                TextButton changeTeam = new TextButton("Change Team", ScrapBox.getInstance().getSkin());
                changeTeam.addListener(new ClickListener(){
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        escapeMenu.remove();
                        SelectBox<String> teamsSelector = new SelectBox<>(ScrapBox.getInstance().getSkin());
                        teamsSelector.setItems(InGameScene.this.teams.toArray(String[]::new));
                        teamsSelector.setSelected(InGameScene.this.playerTeam);
                        Dialog setTeamWindow = new Dialog("Change Team", ScrapBox.getInstance().getSkin(), "dialog"){
                            @Override
                            protected void result(Object object) {
                                escapeMenu.remove();
                                escapeMenu = null;
                                if(object instanceof String){
                                    connection.send(new PlayerTeamUpdate(teamsSelector.getSelected()));
                                }
                            }
                        };
                        setTeamWindow.getContentTable().add(teamsSelector);
                        setTeamWindow.button("Cancel");
                        setTeamWindow.button("Ok", "");
                        setTeamWindow.show(stage);
                        escapeMenu = setTeamWindow;
                    }
                });
                table.add(changeTeam).row();
                if(server != null) {
                    TextButton openToLan = new TextButton("Open to LAN", ScrapBox.getInstance().getSkin());
                    openToLan.setDisabled(server.networkServer!=null);
                    if(server.networkServer == null) {
                        openToLan.addListener(new ClickListener() {
                            @Override
                            public void clicked(InputEvent event, float x, float y) {
                                escapeMenu.remove();
                                TextField input = new TextField("0", ScrapBox.getInstance().getSkin());
                                input.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
                                Dialog openLanMenu = new Dialog("Open to LAN", ScrapBox.getInstance().getSkin(), "dialog") {
                                    @Override
                                    protected void result(Object object) {
                                        escapeMenu.remove();
                                        escapeMenu = null;
                                        if (object instanceof String) {
                                            try {
                                                short port = Short.parseShort(input.getText());
                                                server.startNetwork(port);
                                            } catch (NumberFormatException e) {
                                            }
                                        }
                                    }
                                };
                                openLanMenu.getContentTable().add(new Label("port:", ScrapBox.getInstance().getSkin()));
                                openLanMenu.getContentTable().add(input);
                                openLanMenu.button("Cancel");
                                openLanMenu.button("Ok", "");
                                openLanMenu.show(stage);
                                escapeMenu = openLanMenu;
                            }
                        });
                    }
                    table.add(openToLan).row();
                    TextButton setPasswordButton = new TextButton("Set Server Password", ScrapBox.getInstance().getSkin());
                    setPasswordButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            escapeMenu.remove();
                            TextField input = new TextField("", ScrapBox.getInstance().getSkin());
                            Dialog passwordSet = new Dialog("Set Password", ScrapBox.getInstance().getSkin(), "dialog") {
                                @Override
                                protected void result(Object object) {
                                    escapeMenu.remove();
                                    escapeMenu = null;
                                    if (object instanceof String) {
                                        String password = input.getText().trim();
                                        if (password.isEmpty()) {
                                            server.password = null;
                                        } else {
                                            server.password = password;
                                        }
                                    }
                                }
                            };
                            passwordSet.setMovable(false);
                            passwordSet.button("Cancel");
                            passwordSet.button("Ok", "");
                            passwordSet.getContentTable().add(input);
                            passwordSet.show(stage);
                            escapeMenu = passwordSet;
                        }
                    });
                    table.add(setPasswordButton);
                    table.row();
                }
                TextButton mainMenu = new TextButton("Main Menu", ScrapBox.getInstance().getSkin());
                mainMenu.addListener(new ClickListener(){
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        ScrapBox.getInstance().setScene(new MainMenuScene());
                    }
                });
                table.add(mainMenu);
                escapeMenu.button("Back");
                escapeMenu.show(stage);
            } else {
                escapeMenu.remove();
                escapeMenu = null;
            }
        }

        cameraController.camera.update();
        if(ScrapBox.getSettings().DEBUG_PHYSICS_RENDERING.isJustDown()){
            debugRendering = !debugRendering;
        }
        this.terrainRenderer.draw(this.cameraController);
        batch.setProjectionMatrix(cameraController.camera.combined);
        shapeRenderer.setProjectionMatrix(cameraController.camera.combined);
        batch.begin();
        for (ClientGameObject gameObject : gameObjects.values()) {
            switch (gameObject.mode){
                case Normal:
                    batch.setColor(0.5f, 0.5f, 0.5f, 1);
                    break;
                case Static:
                    batch.setColor(Color.YELLOW);
                    break;
                case Ghost:
                    batch.setColor(0.5f, 0.5f, 0.5f, 0.7f);
                    break;
            }
            RenderData renderData = renderDataRegistry.get(gameObject.type);
            if(renderData.customRenderFunction != null){
                renderData.customRenderFunction.render(renderData, gameObject, batch);
            } else {
                renderData.draw(batch, gameObject);
            }
        }
        batch.end();

        Matrix4 uiMatrix = new Matrix4();
        uiMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        shapeRenderer.setProjectionMatrix(uiMatrix);
        shapeRenderer.setAutoShapeType(true);
        float waterLevel = cameraController.camera.project(new Vector3(0, -100*BOX_TO_PIXELS_RATIO, 0)).y;
        if(waterLevel > 0){
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 1, 0.5f);
            shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), waterLevel);
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        if(!buildAreaRenderer.buildableAreas.isEmpty()) {
            batch.setColor(0.5f, 0.5f, 0.5f, 1);
            buildAreaRenderer.drawFrameBuffer(cameraController.camera);
            batch.setProjectionMatrix(uiMatrix);
            batch.begin();
            TextureRegion buildAreaRegion = new TextureRegion(buildAreaRenderer.frameBuffer.getColorBufferTexture());
            buildAreaRegion.flip(false, true);
            batch.draw(buildAreaRegion, 0, 0);
            batch.end();
            batch.setProjectionMatrix(cameraController.camera.combined);
        }

        if(debugRendering && server != null){
            Matrix4 matrix = cameraController.camera.combined.cpy();
            synchronized (server.physics) {
                try {
                    debugRenderer.render(server.physics, matrix.scl(BOX_TO_PIXELS_RATIO, BOX_TO_PIXELS_RATIO, 0));
                } catch(Exception e){}
            }
        }

        stage.act();
        stage.draw();


        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setProjectionMatrix(cameraController.camera.combined);
        for(ShowActivePossibleWelds.PossibleWeld weld: weldShowcase){
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.rectLine(weld.first.cpy().scl(BOX_TO_PIXELS_RATIO), weld.second.cpy().scl(BOX_TO_PIXELS_RATIO), 5);
        }
        Actor dragging = dragAndDrop.getDragActor();
        if(dragging != null){
            shapeRenderer.setProjectionMatrix(uiMatrix);
            shapeRenderer.setColor(Color.BLACK);
            Vector2 start = ((EditorUILink.ConnectionData)dragAndDrop.getDragPayload().getObject()).position;
            shapeRenderer.rectLine(dragging.getX() + dragging.getWidth(), dragging.getY() + dragging.getHeight()/2, start.x, start.y, 3);
        }
        if(ScrapBox.getSettings().GEAR_CONNECTION.isDown() && gearJointSelection != -1){
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.setProjectionMatrix(cameraController.camera.combined);
            Vector2 firstPos = gameObjects.get(gearJointSelection).getRealPosition();
            MouseSelector.Selection selection = mouseSelector.getSelected(clientGameObject -> clientGameObject.gearJoinable);
            Vector2 secondPos = selection!=null?gameObjects.get(selection.selectionId).getRealPosition():mouseSelector.getWorldMousePosition();
            shapeRenderer.rectLine(firstPos.x * BOX_TO_PIXELS_RATIO, firstPos.y * BOX_TO_PIXELS_RATIO, secondPos.x * BOX_TO_PIXELS_RATIO, secondPos.y * BOX_TO_PIXELS_RATIO, 3);
        }
        if(ScrapBox.getSettings().GEAR_CONNECTION.isDown()){
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.setProjectionMatrix(cameraController.camera.combined);
            batch.setProjectionMatrix(cameraController.camera.combined);
            for(SendConnectionListData.GearConnection gearConnection : gearConnectionsShowcase){
                ClientGameObject objectA = gameObjects.get(gearConnection.goA);
                ClientGameObject objectB = gameObjects.get(gearConnection.goB);
                if(objectA != null & objectB != null){
                    shapeRenderer.rectLine(objectA.getRealPosition().scl(BOX_TO_PIXELS_RATIO), objectB.getRealPosition().scl(BOX_TO_PIXELS_RATIO), 3);
                    shapeRenderer.end();
                    Vector2 textPosition = objectA.getRealPosition().lerp(objectB.getRealPosition(), 0.5f).scl(BOX_TO_PIXELS_RATIO);
                    int ratioA = gearConnection.ratioA;
                    int ratioB = gearConnection.ratioB;
                    float angle = objectB.getRealPosition().sub(objectA.getRealPosition()).angleDeg();
                    if(angle > 90 && angle < 270){
                        int tmp = ratioA;
                        ratioA = ratioB;
                        ratioB = tmp;
                        angle += 180;
                    }
                    BitmapFont font = ScrapBox.getInstance().getSkin().getFont("default-font");
                    font.setColor(Color.WHITE);
                    GlyphLayout layout = font.getCache().addText(ratioA + ":" + ratioB, 0, 0);
                    batch.setTransformMatrix(new Matrix4().rotate(Vector3.Z, angle).trn(textPosition.x, textPosition.y, 0));
                    batch.begin();
                    font.draw(batch, ratioA + ":" + ratioB, -layout.width/2, layout.height);
                    batch.end();
                    batch.setTransformMatrix(new Matrix4());
                    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                }
            }
        }
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if(toolBox.isTerrainSelectionOpen()){
            shapeRenderer.setProjectionMatrix(cameraController.camera.combined);
            shapeRenderer.setColor(Color.BLACK);
            Vector2 position = mouseSelector.getWorldMousePosition();
            if(toolBox.brushRectangle){
                shapeRenderer.rect((position.x - toolBox.brushSize) * BOX_TO_PIXELS_RATIO, (position.y - toolBox.brushSize) * BOX_TO_PIXELS_RATIO, toolBox.brushSize * 2 * BOX_TO_PIXELS_RATIO, toolBox.brushSize * 2 * BOX_TO_PIXELS_RATIO);
            } else {
                shapeRenderer.circle(position.x * BOX_TO_PIXELS_RATIO, position.y * BOX_TO_PIXELS_RATIO, toolBox.brushSize * 2 * BOX_TO_PIXELS_RATIO);
            }
        }
        shapeRenderer.end();
        if(ScrapBox.getSettings().WRENCH.isDown()) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            MouseSelector.Selection selected = mouseSelector.getSelected();
            for(ClientGameObject gameObject : gameObjects.values()){
                float healthPercent = gameObject.health/gameObject.maxHealth;
                float size = (selected != null && gameObject.id == selected.id) ? 1 : 0.5f;
                final float BAR_WIDTH = 200 * size;
                final float BAR_HEIGHT = 50 * size;
                shapeRenderer.setColor(89f/255f, 9f/255f, 0, 1);
                shapeRenderer.rect((gameObject.getRealPosition().x*InGameScene.BOX_TO_PIXELS_RATIO-BAR_WIDTH/2), gameObject.getRealPosition().y*InGameScene.BOX_TO_PIXELS_RATIO-BAR_HEIGHT/2, BAR_WIDTH, BAR_HEIGHT);
                shapeRenderer.setColor(1, 0, 0, 1);
                shapeRenderer.rect(gameObject.getRealPosition().x*InGameScene.BOX_TO_PIXELS_RATIO-BAR_WIDTH/2+BAR_HEIGHT/10, gameObject.getRealPosition().y*InGameScene.BOX_TO_PIXELS_RATIO + BAR_HEIGHT/10 - BAR_HEIGHT/2, (BAR_WIDTH-BAR_HEIGHT/5)*healthPercent, BAR_HEIGHT * 4/5);
            }
            if(selected != null){
                float healthChange = 50*Gdx.graphics.getDeltaTime();
                if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
                    connection.send(new ChangeObjectHealth(selected.id, -healthChange));
                }
                if(Gdx.input.isButtonPressed(Input.Buttons.RIGHT)){
                    connection.send(new ChangeObjectHealth(selected.id, healthChange));
                }
            }
            shapeRenderer.end();
        }
        batch.setColor(0.5f, 0.5f, 0.5f, 1);
        if(toolBox.tool == ToolBox.Tool.Hand && ScrapBox.getSettings().BREAK_CONNECTION.isDown()){
            batch.begin();
            if(ScrapBox.getSettings().GEAR_CONNECTION.isDown()){
                for (SendConnectionListData.GearConnection connection1 : gearConnectionsShowcase) {
                    ClientGameObject objectA = gameObjects.get(connection1.goA);
                    ClientGameObject objectB = gameObjects.get(connection1.goB);
                    if (objectA != null && objectB != null) {
                        Vector2 position = objectA.getRealPosition().lerp(objectB.getRealPosition(), 0.5f).scl(BOX_TO_PIXELS_RATIO);
                        batch.draw(jointBreakIcon, position.x - JOINT_BREAK_ICON_SIZE / 2, position.y - JOINT_BREAK_ICON_SIZE / 2, JOINT_BREAK_ICON_SIZE, JOINT_BREAK_ICON_SIZE);
                    }
                }
            } else {
                for (SendConnectionListData.Connection connection1 : connectionsShowcase) {
                    batch.draw(jointBreakIcon, connection1.position.x * BOX_TO_PIXELS_RATIO - JOINT_BREAK_ICON_SIZE / 2, connection1.position.y * BOX_TO_PIXELS_RATIO - JOINT_BREAK_ICON_SIZE / 2, JOINT_BREAK_ICON_SIZE, JOINT_BREAK_ICON_SIZE);
                }
            }
            batch.end();
        }

        batch.begin();
        batch.setColor(0.5f, 0.5f, 0.5f, 1);
        batch.setProjectionMatrix(uiMatrix);
        toolBox.render(batch);
        batch.end();
        float realWidth = Gdx.graphics.getWidth()-toolBox.getWidth();
        if(controllingData != null){
            float stripWidth = CONTROLLER_BUTTON_SIZE*10 + CONTROLLER_BUTTON_SIZE*9/2;
            batch.begin();
            for(int i = 0;i < 10;i++){
                if(controllerState[i]){
                    batch.setColor(0.5f, 1, 0.5f, 1);
                } else {
                    batch.setColor(0.5f, 0.5f, 0.5f, 1);
                }
                batch.draw(controllerButton, (float) (realWidth/2-stripWidth/2+i*CONTROLLER_BUTTON_SIZE*1.5), CONTROLLER_BUTTON_SIZE, CONTROLLER_BUTTON_SIZE, CONTROLLER_BUTTON_SIZE);
            }
            batch.end();
        }
        if(isPaused){
            batch.begin();
            float width = pausedTexture.getRegionWidth()*3;
            float height = pausedTexture.getRegionHeight()*3;
            batch.draw(pausedTexture, realWidth/2- width/2, Gdx.graphics.getHeight()-50-height, width, height);
            batch.end();
        }

        if(ScrapBox.getSettings().WELD.isJustDown()){
            connection.send(new CommitWeld());
        }
        if(ScrapBox.getSettings().FREEZE.isJustDown()){
            connection.send(new LockGameObject());
        }
        if(ScrapBox.getSettings().EDIT_OBJECT.isJustDown()){
            MouseSelector.Selection selection = mouseSelector.getSelected();
            if(selection != null) {
                connection.send(new OpenGameObjectEditUI(selection.selectionId));
            }
        }
        if(ScrapBox.getSettings().OPEN_CONTROLLER.isJustDown()){
            if(controllingData != null){
                controllingData = null;
            } else {
                MouseSelector.Selection selection = mouseSelector.getSelected();
                if (selection != null && gameObjects.get(selection.selectionId).type.equals("controller")) {
                    controllingData = new ControllingData(selection.selectionId);
                }
            }
        }
        connection.send(new MouseMoved(mouseSelector.getWorldMousePosition()));
    }
    public void closeEditor(ClientGameObjectEditor editor){
        editors.entrySet().removeIf(entry -> entry.getValue() == editor);
        editor.window.remove();
        editor.dispose();
    }
    @Override
    public void resize(int width, int height) {
        this.stage.getViewport().setWorldSize(width, height);
        this.stage.getViewport().update(width, height, true);
        Vector3 position = cameraController.camera.position.cpy();
        cameraController.camera.setToOrtho(false, width, height);
        cameraController.camera.position.set(position);
        buildAreaRenderer.resize(width, height);
    }
    @Override
    public void dispose() {
        if(server != null){
            server.stop();
        }
        if(client != null){
            try {
                client.disconnect();
            } catch(Exception e){}
        }
        batch.dispose();
        toolBox.dispose();
        jointBreakIcon.dispose();
        editors.forEach((integer, editor) -> editor.dispose());
        controllerButton.dispose();
        for(RenderData renderData : renderDataRegistry.values()){
            renderData.dispose();
        }
    }
    private static class ControllingData{
        public final int controllingId;
        private ControllingData(int controllingId) {
            this.controllingId = controllingId;
        }
    }
}
