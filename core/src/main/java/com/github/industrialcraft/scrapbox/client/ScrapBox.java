package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.github.industrialcraft.scrapbox.common.net.msg.*;
import com.github.industrialcraft.scrapbox.server.Server;
import com.github.industrialcraft.scrapbox.common.net.LocalClientConnection;
import com.github.industrialcraft.scrapbox.common.net.MessageS2C;

import java.util.HashMap;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class ScrapBox extends ApplicationAdapter {
    public static final float BOX_TO_PIXELS_RATIO = 100;
    private SpriteBatch batch;
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
    @Override
    public void create() {
        cameraController = new CameraController(new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        debugRenderer = new Box2DDebugRenderer();
        renderDataRegistry = new HashMap<>();
        renderDataRegistry.put("frame", new RenderData(new Texture("wooden_frame.png"), 1, 1));
        batch = new SpriteBatch();
        server = new Server();
        gameObjects = new HashMap<>();
        connection = server.joinLocalPlayer();
        server.start();
        debugRendering = false;
        mouseSelector = new MouseSelector(this);
        this.toolBox = new ToolBox(this);
        renderDataRegistry.forEach((key, value) -> this.toolBox.addPart(key, value));
        Gdx.input.setInputProcessor(new InputProcessor() {
            @Override
            public boolean keyDown(int keycode) {
                if(keycode == Input.Keys.Q && selected != null){
                    connection.send(new PinchingSetGhost(true));
                }
                return false;
            }
            @Override
            public boolean keyUp(int keycode) {
                if(keycode == Input.Keys.Q && selected != null){
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
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
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
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.F2)){
            connection.send(new ToggleGamePaused());
        }
        cameraController.tick();
        cameraController.camera.update();
        if(Gdx.input.isKeyJustPressed(Input.Keys.F1)){
            debugRendering = !debugRendering;
        }
        batch.setProjectionMatrix(cameraController.camera.combined);
        batch.begin();
        for (ClientGameObject gameObject : gameObjects.values()) {
            renderDataRegistry.get(gameObject.type).draw(batch, gameObject);
        }
        Matrix4 uiMatrix = new Matrix4();
        uiMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setProjectionMatrix(uiMatrix);
        toolBox.render(batch);
        if(selected != null){
            batch.setProjectionMatrix(cameraController.camera.combined);
            ClientGameObject selectedGameObject = gameObjects.get(selected.id);
            if(selectedGameObject != null) {
                renderDataRegistry.get(selectedGameObject.type).draw(batch, selectedGameObject);
            }
        }
        batch.end();
        if(debugRendering){
            Matrix4 matrix = cameraController.camera.combined.cpy();
            debugRenderer.render(server.physics, matrix.scl(BOX_TO_PIXELS_RATIO, BOX_TO_PIXELS_RATIO, 0));
        }
        connection.send(new MouseMoved(mouseSelector.getWorldMousePosition()));
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
