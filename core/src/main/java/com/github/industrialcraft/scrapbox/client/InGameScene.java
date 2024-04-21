package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Predicate;
import com.github.industrialcraft.netx.NetXClient;
import com.github.industrialcraft.scrapbox.common.editui.EditorUILink;
import com.github.industrialcraft.scrapbox.common.net.IConnection;
import com.github.industrialcraft.scrapbox.common.net.msg.*;
import com.github.industrialcraft.scrapbox.server.Server;
import com.github.industrialcraft.scrapbox.server.game.FrameGameObject;
import com.github.tommyettinger.colorful.rgb.ColorfulBatch;

import java.util.ArrayList;
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
    private Texture jointBreakIcon;
    private boolean[] controllerState;
    private Texture controllerButton;
    private BitmapFont font;
    private static final float JOINT_BREAK_ICON_SIZE = 48;
    private static final float CONTROLLER_BUTTON_SIZE = 80;
    public InGameScene(IConnection connection, Server server, NetXClient client) {
        this.connection = connection;
        this.server = server;
        this.client = client;
    }
    @Override
    public void create() {
        this.dragAndDrop = new DragAndDrop();
        font = new BitmapFont();
        stage = new Stage();
        editors = new HashMap<>();
        cameraController = new CameraController(this, new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        debugRenderer = new Box2DDebugRenderer();
        renderDataRegistry = new HashMap<>();
        renderDataRegistry.put("frame", new RenderData(new Texture("wooden_frame.png"), 1, 1));
        renderDataRegistry.put("wheel", new RenderData(new Texture("wooden_wheel.png"), 1, 1));
        renderDataRegistry.put("wheel_join", new RenderData(new Texture("wheel_join.png"), 1, 1));
        renderDataRegistry.put("balloon", new RenderData(new Texture("balloon.png"), 1, 1));
        renderDataRegistry.put("puncher_box", new RenderData(new Texture("puncher_box.png"), FrameGameObject.INSIDE_SIZE, FrameGameObject.INSIDE_SIZE));
        renderDataRegistry.put("puncher", new RenderData(new Texture("puncher.png"), 1, 1));
        renderDataRegistry.put("controller", new RenderData(new Texture("controller.png"), FrameGameObject.INSIDE_SIZE, FrameGameObject.INSIDE_SIZE));
        renderDataRegistry.put("propeller", new RenderData(new Texture("propeller.png"), 1, 0.25f));
        renderDataRegistry.put("tnt", new RenderData(new Texture("tnt.png"), 1, 1));
        renderDataRegistry.put("rotator_join", new RenderData(new Texture("rotator_join.png"), 1, 1));
        renderDataRegistry.put("rotator_end", new RenderData(new Texture("rotator_end.png"), 1, 1));
        renderDataRegistry.put("cannon", new RenderData(new Texture("cannon.png"), 1, 1));
        renderDataRegistry.put("bullet", new RenderData(new Texture("bullet.png"), 0.1f, 0.1f));
        renderDataRegistry.put("position_sensor", new RenderData(new Texture("position_sensor.png"), FrameGameObject.INSIDE_SIZE, FrameGameObject.INSIDE_SIZE));
        renderDataRegistry.put("display", new RenderData(new Texture("display.png"), FrameGameObject.INSIDE_SIZE, FrameGameObject.INSIDE_SIZE, (gameObject, batch) -> {
            Matrix4 mx4Font = new Matrix4();
            Vector3 translation = new Vector3(gameObject.getRealPosition().x * BOX_TO_PIXELS_RATIO, gameObject.getRealPosition().y * BOX_TO_PIXELS_RATIO, 0);
            mx4Font.translate(translation).rotateRad(new Vector3(0, 0, 1), gameObject.rotation).translate(translation.cpy().scl(-1));
            batch.setTransformMatrix(mx4Font);
            GlyphLayout layout = new GlyphLayout();
            layout.setText(font, gameObject.animationData);
            float realWidth = Math.min(layout.width, FrameGameObject.INSIDE_SIZE*2);
            font.draw(batch, gameObject.animationData, translation.x - layout.width / 2, translation.y);
            mx4Font.idt();
            batch.setTransformMatrix(mx4Font);
        }));
        batch = new ColorfulBatch();
        gameObjects = new HashMap<>();
        debugRendering = false;
        mouseSelector = new MouseSelector(this);
        this.toolBox = new ToolBox(this);
        this.toolBox.addPart("frame", renderDataRegistry.get("frame"));
        this.toolBox.addPart("wheel", renderDataRegistry.get("wheel"));
        this.toolBox.addPart("balloon", renderDataRegistry.get("balloon"));
        this.toolBox.addPart("controller", renderDataRegistry.get("controller"));
        this.toolBox.addPart("puncher", renderDataRegistry.get("puncher_box"));
        this.toolBox.addPart("propeller", renderDataRegistry.get("propeller"));
        this.toolBox.addPart("tnt", renderDataRegistry.get("tnt"));
        this.toolBox.addPart("rotator", renderDataRegistry.get("rotator_join"));
        this.toolBox.addPart("cannon", renderDataRegistry.get("cannon"));
        this.toolBox.addPart("position_sensor", renderDataRegistry.get("position_sensor"));
        this.toolBox.addPart("display", renderDataRegistry.get("display"));
        this.weldShowcase = new ArrayList<>();
        this.shapeRenderer = new ShapeRenderer();
        this.terrainRenderer = new TerrainRenderer();
        addTerrainType("dirt", "dirt.png");
        addTerrainType("stone", "stone.png");
        addTerrainType("ice", "ice.png");
        addTerrainType("slime", "slime.png");
        this.connectionsShowcase = new ArrayList<>();
        this.jointBreakIcon = new Texture("joint_break_icon.png");
        this.controllerState = new boolean[10];
        this.controllerButton = new Texture("controller_button.png");
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, new InputProcessor() {
            @Override
            public boolean keyDown(int keycode) {
                if(keycode == Input.Keys.Q){
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
                    if(toolBox.tool == ToolBox.Tool.Hand) {
                        selected = mouseSelector.getSelected();
                        if (selected != null) {
                            connection.send(new GameObjectPinch(selected.id, new Vector2(selected.offsetX, selected.offsetY)));
                        }
                    }
                    if(toolBox.tool == ToolBox.Tool.DeleteJoints){
                        Vector3 mouse = cameraController.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
                        Vector2 mouse2 = new Vector2(mouse.x, mouse.y);
                        for(SendConnectionListData.Connection connection1 : connectionsShowcase){
                            if(mouse2.dst(connection1.position.cpy().scl(BOX_TO_PIXELS_RATIO)) < JOINT_BREAK_ICON_SIZE/2){
                                connection.send(new DestroyJoint(connection1.gameObjectId, connection1.name));
                            }
                        }
                    }
                } else {
                    toolBox.click(new Vector2(screenX, Gdx.graphics.getHeight()-screenY));
                }
                return false;
            }
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if(selected != null){
                    connection.send(new GameObjectRelease());
                    if(toolBox.isMouseInside()){
                        connection.send(new TrashObject(selected.id));
                    }
                }
                selected = null;
                return false;
            }
            @Override
            public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
                return false;
            }
            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if(!toolBox.isMouseInside()) {
                    if (toolBox.tool == ToolBox.Tool.TerrainPlace) {
                        connection.send(new PlaceTerrain(toolBox.getSelectedTerrainType(), mouseSelector.getWorldMousePosition(), 2));
                    }
                    if (toolBox.tool == ToolBox.Tool.TerrainDestroy) {
                        connection.send(new PlaceTerrain("", mouseSelector.getWorldMousePosition(), 2));
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
                if(selected != null){
                    connection.send(new PinchingRotate(amountY));
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
                gameObjects.remove(deleteGameObject.id);
                ClientGameObjectEditor editor = editors.get(deleteGameObject.id);
                if(editor != null)
                    closeEditor(editor);
            }
            if(message instanceof TakeObjectResponse){
                TakeObjectResponse takeObjectResponse = (TakeObjectResponse) message;
                selected = new MouseSelector.Selection(takeObjectResponse.id, takeObjectResponse.offset.x, takeObjectResponse.offset.y, 0);
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
            }
            if(message instanceof ResponseControllerState){
                ResponseControllerState responseControllerState = (ResponseControllerState) message;
                this.controllerState = responseControllerState.state;
            }
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.F2)){
            connection.send(new ToggleGamePaused());
        }
        if(controllingData != null){
            connection.send(new RequestControllerState(controllingData.controllingId));
            ClientGameObject gameObject = gameObjects.get(controllingData.controllingId);
            if(gameObject == null){
                cameraController.camera.position.set(controllingData.position.x, controllingData.position.y, 0);
                controllingData = null;
            } else {
                Vector2 lerpedPosition = gameObject.getRealPosition();
                cameraController.camera.position.set(lerpedPosition.x * BOX_TO_PIXELS_RATIO, lerpedPosition.y * BOX_TO_PIXELS_RATIO, 0);
            }
        } else {
            cameraController.tick();
        }
        cameraController.camera.update();
        if(Gdx.input.isKeyJustPressed(Input.Keys.F1)){
            debugRendering = !debugRendering;
        }
        this.terrainRenderer.draw(this.cameraController);
        drawObjects(arg0 -> true);

        stage.act();
        stage.draw();

        Matrix4 uiMatrix = new Matrix4();
        uiMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        shapeRenderer.setProjectionMatrix(cameraController.camera.combined);
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
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
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if(toolBox.isTerrainSelectionOpen()){
            shapeRenderer.setProjectionMatrix(cameraController.camera.combined);
            shapeRenderer.setColor(Color.BLACK);
            Vector2 position = mouseSelector.getWorldMousePosition();
            shapeRenderer.circle(position.x * BOX_TO_PIXELS_RATIO, position.y * BOX_TO_PIXELS_RATIO, 2 * BOX_TO_PIXELS_RATIO);
        }

        shapeRenderer.end();

        batch.begin();
        batch.setColor(0.5f, 0.5f, 0.5f, 1);
        batch.setProjectionMatrix(uiMatrix);
        toolBox.render(batch);
        batch.end();
        if(controllingData != null){
            float realWidth = Gdx.graphics.getWidth()-toolBox.getWidth();
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

        drawObjects(arg0 -> arg0.selected);

        if(debugRendering && server != null){
            Matrix4 matrix = cameraController.camera.combined.cpy();
            synchronized (server.physics) {
                debugRenderer.render(server.physics, matrix.scl(BOX_TO_PIXELS_RATIO, BOX_TO_PIXELS_RATIO, 0));
            }
        }

        if(toolBox.tool == ToolBox.Tool.DeleteJoints){
            batch.begin();
            for(SendConnectionListData.Connection connection1 : connectionsShowcase){
                batch.draw(jointBreakIcon, connection1.position.x * BOX_TO_PIXELS_RATIO - JOINT_BREAK_ICON_SIZE/2, connection1.position.y * BOX_TO_PIXELS_RATIO - JOINT_BREAK_ICON_SIZE/2, JOINT_BREAK_ICON_SIZE, JOINT_BREAK_ICON_SIZE);
            }
            batch.end();
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.F)){
            connection.send(new CommitWeld());
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.X)){
            connection.send(new LockGameObject());
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.V)){
            MouseSelector.Selection selection = mouseSelector.getSelected();
            if(selection != null) {
                connection.send(new OpenGameObjectEditUI(selection.id));
            }
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.C)){
            if(controllingData != null){
                cameraController.camera.position.set(controllingData.position.x, controllingData.position.y, 0);
                controllingData = null;
            } else {
                MouseSelector.Selection selection = mouseSelector.getSelected();
                if (selection != null && gameObjects.get(selection.id).type.equals("controller")) {
                    controllingData = new ControllingData(new Vector2(cameraController.camera.position.x, cameraController.camera.position.y), selection.id);
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
    public void drawObjects(Predicate<ClientGameObject> predicate){
        batch.setProjectionMatrix(cameraController.camera.combined);
        batch.begin();
        for (ClientGameObject gameObject : gameObjects.values()) {
            if(!predicate.evaluate(gameObject)){
                continue;
            }
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
            renderData.draw(batch, gameObject);
            if(renderData.customRenderFunction != null){
                renderData.customRenderFunction.render(gameObject, batch);
            }
        }
        batch.end();
    }
    @Override
    public void resize(int width, int height) {
        this.stage.getViewport().setWorldSize(width, height);
        this.stage.getViewport().update(width, height, true);
        Vector3 position = cameraController.camera.position.cpy();
        cameraController.camera.setToOrtho(false, width, height);
        cameraController.camera.position.set(position);
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
        public final Vector2 position;
        public final int controllingId;
        private ControllingData(Vector2 position, int controllingId) {
            this.position = position;
            this.controllingId = controllingId;
        }
    }
}
