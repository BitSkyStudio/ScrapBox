package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Predicate;
import com.github.industrialcraft.netx.NetXClient;
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
    private ToolBox toolBox;
    private ArrayList<ShowActivePossibleWelds.PossibleWeld> weldShowcase;
    private ShapeRenderer shapeRenderer;
    private TerrainRenderer terrainRenderer;
    public Stage stage;
    public HashMap<Integer,ClientGameObjectEditor> editors;
    public DragAndDrop dragAndDrop;
    private ControllingData controllingData;
    public InGameScene(IConnection connection, Server server, NetXClient client) {
        this.connection = connection;
        this.server = server;
        this.client = client;
    }
    @Override
    public void create() {
        this.dragAndDrop = new DragAndDrop();
        stage = new Stage();
        editors = new HashMap<>();
        cameraController = new CameraController(new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        debugRenderer = new Box2DDebugRenderer();
        renderDataRegistry = new HashMap<>();
        renderDataRegistry.put("frame", new RenderData(new Texture("wooden_frame.png"), 1, 1));
        renderDataRegistry.put("wheel", new RenderData(new Texture("wooden_wheel.png"), 1, 1));
        renderDataRegistry.put("wheel_join", new RenderData(new Texture("wheel_join.png"), 1, 1));
        renderDataRegistry.put("balloon", new RenderData(new Texture("balloon.png"), 1, 1));
        renderDataRegistry.put("controller", new RenderData(new Texture("controller.png"), FrameGameObject.INSIDE_SIZE, FrameGameObject.INSIDE_SIZE));
        batch = new ColorfulBatch();
        gameObjects = new HashMap<>();
        debugRendering = false;
        mouseSelector = new MouseSelector(this);
        this.toolBox = new ToolBox(this);
        this.toolBox.addPart("frame", renderDataRegistry.get("frame"));
        this.toolBox.addPart("wheel", renderDataRegistry.get("wheel"));
        this.toolBox.addPart("balloon", renderDataRegistry.get("balloon"));
        this.toolBox.addPart("controller", renderDataRegistry.get("controller"));
        this.weldShowcase = new ArrayList<>();
        this.shapeRenderer = new ShapeRenderer();
        this.terrainRenderer = new TerrainRenderer();
        this.terrainRenderer.addTerrainType("dirt", "dirt.png");
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, new InputProcessor() {
            @Override
            public boolean keyDown(int keycode) {
                if(keycode == Input.Keys.Q){
                    connection.send(new PinchingGhostToggle());
                }
                return false;
            }
            @Override
            public boolean keyUp(int keycode) {
                return false;
            }
            @Override
            public boolean keyTyped(char character) {
                return false;
            }
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if(!toolBox.isMouseInside()) {
                    selected = mouseSelector.getSelected();
                    if (selected != null) {
                        connection.send(new GameObjectPinch(selected.id, new Vector2(selected.offsetX, selected.offsetY)));
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
                if(toolBox.tool == ToolBox.Tool.TerrainPlace){
                    connection.send(new PlaceTerrain(mouseSelector.getWorldMousePosition(), 2));
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
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.F2)){
            connection.send(new ToggleGamePaused());
        }
        if(controllingData != null){
            ClientGameObject gameObject = gameObjects.get(controllingData.controllingId);
            if(gameObject == null){
                cameraController.camera.position.set(controllingData.position.x, controllingData.position.y, 0);
                controllingData = null;
            } else {
                cameraController.camera.position.set(gameObject.position.x * BOX_TO_PIXELS_RATIO, gameObject.position.y * BOX_TO_PIXELS_RATIO, 0);
            }
        } else {
            cameraController.tick();
        }
        cameraController.camera.update();
        if(Gdx.input.isKeyJustPressed(Input.Keys.F1)){
            debugRendering = !debugRendering;
        }
        drawObjects(arg0 -> true);

        this.terrainRenderer.draw(this.cameraController);

        shapeRenderer.setProjectionMatrix(cameraController.camera.combined);
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for(ShowActivePossibleWelds.PossibleWeld weld: weldShowcase){
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.rectLine(weld.first.cpy().scl(BOX_TO_PIXELS_RATIO), weld.second.cpy().scl(BOX_TO_PIXELS_RATIO), 5);
        }
        shapeRenderer.end();

        batch.begin();
        batch.setColor(0.5f, 0.5f, 0.5f, 1);
        Matrix4 uiMatrix = new Matrix4();
        uiMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setProjectionMatrix(uiMatrix);
        toolBox.render(batch);
        batch.end();

        drawObjects(arg0 -> arg0.selected);

        if(debugRendering && server != null){
            Matrix4 matrix = cameraController.camera.combined.cpy();
            debugRenderer.render(server.physics, matrix.scl(BOX_TO_PIXELS_RATIO, BOX_TO_PIXELS_RATIO, 0));
        }

        stage.act();
        stage.draw();

        if(Gdx.input.isKeyJustPressed(Input.Keys.F)){
            connection.send(new CommitWeld());
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.X)){
            connection.send(new LockGameObject());
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.V)){
            connection.send(new OpenGameObjectEditUI(mouseSelector.getSelected().id));
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.C)){
            if(controllingData != null){
                cameraController.camera.position.set(controllingData.position.x, controllingData.position.y, 0);
                controllingData = null;
            } else {
                MouseSelector.Selection selection = mouseSelector.getSelected();
                if (selection != null) {
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
            renderDataRegistry.get(gameObject.type).draw(batch, gameObject);
        }
        batch.end();
    }
    @Override
    public void resize(int width, int height) {
        this.stage.getViewport().update(width, height);
        cameraController.camera.setToOrtho(false, width, height);
        cameraController.camera.position.set(0, 0, 0);
    }
    @Override
    public void dispose() {
        if(server != null){
            server.stop();
        }
        if(client != null){
            client.disconnect();
        }
        batch.dispose();
        toolBox.dispose();
        editors.forEach((integer, editor) -> editor.dispose());
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
