package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.github.industrialcraft.scrapbox.common.net.IConnection;
import com.github.industrialcraft.scrapbox.server.Server;

public class MainMenuScene implements IScene {
    private Stage stage;
    @Override
    public void create() {
        Skin skin = ScrapBox.getInstance().getSkin();
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        TextButton startGameButton = new TextButton("Start Game", skin);
        startGameButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Server server = new Server();
                IConnection connection = server.joinLocalPlayer();
                server.start();
                ScrapBox.getInstance().setScene(new InGameScene(connection, server, null));
            }
        });
        table.add(startGameButton);
        TextButton serverListButton = new TextButton("Server List", skin);
        serverListButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScrapBox.getInstance().setScene(new ServerJoinScene());
            }
        });
        table.add(serverListButton);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(50f / 255f, 50f / 255f, 50f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
