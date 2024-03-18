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
    private LocalClientConnection connection;
    public HashMap<Integer,ClientGameObject> gameObjects;
    public HashMap<String, RenderData> renderDataRegistry;
    private MouseSelector mouseSelector;
    private boolean debugRendering;
    private MouseSelector.Selection selected;
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
        Gdx.input.setInputProcessor(new InputProcessor() {
            @Override
            public boolean keyDown(int keycode) {
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
                selected = mouseSelector.getSelected();
                if(selected != null){
                    connection.send(new GameObjectPinch(selected.id, new Vector2(selected.offsetX, selected.offsetY)));
                }
                return false;
            }
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if(selected != null){
                    connection.send(new GameObjectRelease());
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
                return false;
            }
            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                return false;
            }
            @Override
            public boolean scrolled(float amountX, float amountY) {
                cameraController.zoom(amountY);
                return false;
            }
        });
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
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
            RenderData renderData = renderDataRegistry.get(gameObject.type);
            batch.draw(renderData.texture, (gameObject.position.x - renderData.width) * BOX_TO_PIXELS_RATIO, (gameObject.position.y - renderData.height) * BOX_TO_PIXELS_RATIO, renderData.width * BOX_TO_PIXELS_RATIO, renderData.height * BOX_TO_PIXELS_RATIO, renderData.width * BOX_TO_PIXELS_RATIO * 2, renderData.height * BOX_TO_PIXELS_RATIO * 2, 1, 1, (float) Math.toDegrees(gameObject.rotation));
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
    }
}
