package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.industrialcraft.scrapbox.server.Server;
import com.github.industrialcraft.scrapbox.server.net.LocalClientConnection;
import com.github.industrialcraft.scrapbox.server.net.MessageS2C;
import com.github.industrialcraft.scrapbox.server.net.msg.AddGameObjectMessage;
import com.github.industrialcraft.scrapbox.server.net.msg.MoveGameObjectMessage;

import java.util.HashMap;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class ScrapBox extends ApplicationAdapter {
    private static final float BOX_TO_PIXELS_RATIO = 100;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private CameraController cameraController;
    private Server server;
    private LocalClientConnection connection;
    private HashMap<Integer,ClientGameObject> gameObjects;
    private HashMap<String, RenderData> renderDataRegistry;
    @Override
    public void create() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cameraController = new CameraController(camera);
        renderDataRegistry = new HashMap<>();
        renderDataRegistry.put("frame", new RenderData(new Texture("wooden_frame.png"), 1, 1));
        batch = new SpriteBatch();
        server = new Server();
        gameObjects = new HashMap<>();
        connection = server.joinLocalPlayer();
        server.start();
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
        cameraController.tick();
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for(ClientGameObject gameObject : gameObjects.values()){
            RenderData renderData = renderDataRegistry.get(gameObject.type);
            batch.draw(renderData.texture, (gameObject.position.x - renderData.width/2) * BOX_TO_PIXELS_RATIO, (gameObject.position.y - renderData.height/2) * BOX_TO_PIXELS_RATIO, gameObject.position.x * BOX_TO_PIXELS_RATIO, gameObject.position.y * BOX_TO_PIXELS_RATIO, renderData.width * BOX_TO_PIXELS_RATIO, renderData.height * BOX_TO_PIXELS_RATIO, 1, 1, gameObject.rotation);
        }
        batch.end();
    }
    @Override
    public void resize(int width, int height) {
        this.camera.setToOrtho(false, width, height);
        camera.position.set(0, 0, 0);
    }
    @Override
    public void dispose() {
        server.stop();
        batch.dispose();
    }
}
