package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.github.industrialcraft.netx.NetXClient;
import com.github.industrialcraft.scrapbox.ClientNetXConnection;
import com.github.industrialcraft.scrapbox.common.net.IConnection;

public class ConnectingScene implements IScene {
    public final IConnection connection;
    public final NetXClient netXClient;

    private Stage stage;

    public ConnectingScene(IConnection connection, NetXClient netXClient) {
        this.connection = connection;
        this.netXClient = netXClient;
    }

    @Override
    public void create() {
        Skin skin = ScrapBox.getInstance().getSkin();
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        Label connectingText = new Label("Connecting", skin);
        table.add(connectingText);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(50f / 255f, 50f / 255f, 50f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();
        ((ClientNetXConnection)connection).waitForConnect();
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
