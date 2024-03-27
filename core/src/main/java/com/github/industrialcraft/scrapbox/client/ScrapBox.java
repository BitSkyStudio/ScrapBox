package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Predicate;
import com.badlogic.gdx.utils.ShortArray;
import com.github.industrialcraft.scrapbox.common.net.msg.*;
import com.github.industrialcraft.scrapbox.server.Server;
import com.github.industrialcraft.scrapbox.common.net.LocalClientConnection;
import com.github.industrialcraft.scrapbox.common.net.MessageS2C;
import com.github.tommyettinger.colorful.rgb.ColorfulBatch;

import java.util.ArrayList;
import java.util.HashMap;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class ScrapBox extends ApplicationAdapter {
    public static final float BOX_TO_PIXELS_RATIO = 100;
    private ColorfulBatch batch;
    public CameraController cameraController;
    private Box2DDebugRenderer debugRenderer;
    private Server server;
    public LocalClientConnection connection;
    public HashMap<Integer,ClientGameObject> gameObjects;
    public HashMap<String, RenderData> renderDataRegistry;
    public MouseSelector mouseSelector;
    private boolean debugRendering;
    private MouseSelector.Selection selected;
    private ToolBox toolBox;
    private ArrayList<ShowActivePossibleWelds.PossibleWeld> weldShowcase;
    private ShapeRenderer shapeRenderer;
    private TerrainRenderer terrainRenderer;
    @Override
    public void create() {
        cameraController = new CameraController(new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        debugRenderer = new Box2DDebugRenderer();
        renderDataRegistry = new HashMap<>();
        renderDataRegistry.put("frame", new RenderData(new Texture("wooden_frame.png"), 1, 1));
        renderDataRegistry.put("wheel", new RenderData(new Texture("wooden_wheel.png"), 1, 1));
        renderDataRegistry.put("wheel_join", new RenderData(new Texture("wheel_join.png"), 1, 1));
        batch = new ColorfulBatch();
        server = new Server();
        gameObjects = new HashMap<>();
        connection = server.joinLocalPlayer();
        server.start();
        debugRendering = false;
        mouseSelector = new MouseSelector(this);
        this.toolBox = new ToolBox(this);
        this.toolBox.addPart("frame", renderDataRegistry.get("frame"));
        this.toolBox.addPart("wheel", renderDataRegistry.get("wheel"));
        this.weldShowcase = new ArrayList<>();
        this.shapeRenderer = new ShapeRenderer();
        this.terrainRenderer = new TerrainRenderer();
        this.terrainRenderer.addTerrainType("dirt", "dirt.png");
        Gdx.input.setInputProcessor(new InputProcessor() {
            @Override
            public boolean keyDown(int keycode) {
                if(keycode == Input.Keys.Q){
                    connection.send(new PinchingSetGhost(true));
                }
                return false;
            }
            @Override
            public boolean keyUp(int keycode) {
                if(keycode == Input.Keys.Q){
                    connection.send(new PinchingSetGhost(false));
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
                    connection.send(new PlaceTerrain(mouseSelector.getWorldMousePosition(), 1));
                }
                return false;
            }
            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                return false;
            }
            @Override
            public boolean scrolled(float amountX, float amountY) {
                if(toolBox.isMouseInside()){
                    toolBox.scroll((int) amountY);
                } else {
                    cameraController.zoom(amountY);
                }
                return false;
            }
        });
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(79f / 255f, 201f / 255f, 232f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        for(MessageS2C message : connection.read()){
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
                selected = new MouseSelector.Selection(takeObjectResponse.id, takeObjectResponse.offset.x, takeObjectResponse.offset.y);
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
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.F2)){
            connection.send(new ToggleGamePaused());
        }
        cameraController.tick();
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

        if(debugRendering){
            Matrix4 matrix = cameraController.camera.combined.cpy();
            debugRenderer.render(server.physics, matrix.scl(BOX_TO_PIXELS_RATIO, BOX_TO_PIXELS_RATIO, 0));
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.F)){
            connection.send(new CommitWeld());
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.X)){
            connection.send(new LockGameObject());
        }
        connection.send(new MouseMoved(mouseSelector.getWorldMousePosition()));
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
        cameraController.camera.setToOrtho(false, width, height);
        cameraController.camera.position.set(0, 0, 0);
    }
    @Override
    public void dispose() {
        server.stop();
        batch.dispose();
        toolBox.dispose();
        for(RenderData renderData : renderDataRegistry.values()){
            renderData.dispose();
        }
    }
}
