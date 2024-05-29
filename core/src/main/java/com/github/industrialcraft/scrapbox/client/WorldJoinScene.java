package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.github.industrialcraft.scrapbox.common.net.IConnection;
import com.github.industrialcraft.scrapbox.common.net.msg.EditorUIInput;
import com.github.industrialcraft.scrapbox.server.SaveFile;
import com.github.industrialcraft.scrapbox.server.Server;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

public class WorldJoinScene extends StageBasedScreen {
    @Override
    public void create() {
        super.create();
        Skin skin = ScrapBox.getInstance().getSkin();
        List<String> list = new List<>(skin);
        ScrollPane scrollPane = new ScrollPane(list, skin);
        scrollPane.addListener(new InputListener() {
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                stage.setScrollFocus(scrollPane);
            }
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                stage.setScrollFocus(null);
            }
        });
        String[] saves = new File("./saves").list();
        Array<String> array = new Array<>();
        for(int i = 0;i < saves.length;i++){
            array.add(saves[i].replace(".sbs", ""));
        }
        list.setItems(array);
        table.add(scrollPane);
        table.row();
        Table buttons = new Table();
        TextButton joinButton = new TextButton("Join", skin);
        joinButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(list.getSelected() == null)
                    return;
                File saveFile = new File("./saves/" + list.getSelected() + ".sbs");
                Server server = new Server(saveFile);
                try{
                    FileInputStream fileInputStream = new FileInputStream(saveFile);
                    server.loadSaveFile(new SaveFile(new DataInputStream(fileInputStream)));
                    fileInputStream.close();
                } catch(Exception e){
                    e.printStackTrace();
                    System.out.println("couldn't load");
                    ScrapBox.getInstance().setScene(new MainMenuScene());
                    server.stop();
                    return;
                }
                IConnection connection = server.joinLocalPlayer();
                server.start();
                ScrapBox.getInstance().setScene(new InGameScene(connection, server, null));
            }
        });
        buttons.add(joinButton);
        TextButton createButton = new TextButton("Create", skin);
        createButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                TextField input = new TextField("", skin);
                Dialog dialog = new Dialog("Create World", skin, "dialog") {
                    public void result(Object obj) {
                        if(obj instanceof String && !input.getText().isEmpty()){
                            File saveFile = new File("./saves/" + input.getText() + ".sbs");
                            Server server = new Server(saveFile);
                            IConnection connection = server.joinLocalPlayer();
                            server.start();
                            ScrapBox.getInstance().setScene(new InGameScene(connection, server, null));
                        }
                    }
                };
                dialog.button("Cancel");
                dialog.button("Ok", "");
                dialog.getContentTable().add(input);
                dialog.show(stage);
            }
        });
        buttons.add(createButton);
        table.add(buttons);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
